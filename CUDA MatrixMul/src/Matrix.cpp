

#include <cstdlib>
#include <ctime>

#include <cuda.h>


#include "Math.hpp"
#include "Thread.hpp"

#include "Matrix.hpp"
#include "MatrixMT.hpp"
#include "MatrixCUDA.hpp"


class StopWatch {

private:

    static const double sm_clkPerSecRec;

public:

    StopWatch()
    {
        start();
    }

public:

    void start()
    {
        clock_t clkStart;
        // Ensure we start measuring time right after a tick
        // (and not anywhere between two ticks).
        const clock_t clk = clock();
        do {
            clkStart = clock();
        } while (clk == clkStart);
        m_clkStart = clkStart; 
    }

    void finish(const char * const desc)
    {
        clock_t clkEnd = clock();
        clock_t clkDiff = clkEnd - m_clkStart;
        double runtimeSec = clkDiff * sm_clkPerSecRec;
        unsigned int runtimeMin = static_cast< unsigned int >(runtimeSec) / 60;
        runtimeSec -= runtimeMin * 60;
        printf("done in %u min %.2f sec", runtimeMin, runtimeSec);
        if ((0 == runtimeMin) && (runtimeSec < 0.01)) {
            printf(" (clocks: %lu)", static_cast< unsigned long >(clkDiff));
        }
        printf("\n");
    }

private:

    clock_t m_clkStart;

}; // class StopWatch

/* static */ const double StopWatch::sm_clkPerSecRec = 1.0 / CLOCKS_PER_SEC;


typedef void (* test_func)(
    const float * const a,
    const float * const b,
    float * const x,
    const int n,
    const size_t param);


void test(
    const char * const desc,
    test_func func,
    const float * const a,
    const float * const b,
    float * const x,
    const int n,
    const size_t param)
{
    printf("Running %s test ... ", desc);
    fflush(stdout);
    StopWatch meter;
    // Run the test.
    func(a, b, x, n, param);
    meter.finish(desc);
}


bool testEqual(const float a, const float b, const char * desc, const int row, const int col)
{
    if (!epsilonEquals(a, b)) {
        fprintf(stderr, "%s result differs on position [%d, %d]: (%g) != (%g)\n",
            desc, row, col, a, b);
        return false;
    }
    return true;
}


// Beginning of GPU Architecture definitions
int convertSMVer2Cores(int major, int minor)
{
    // Defines for GPU Architecture types (using the SM version to determine the # of cores per SM
    typedef struct {
        int SM; // 0xMm (hexidecimal notation), M = SM Major version, and m = SM minor version
        int Cores;
    } sSMtoCores;

    sSMtoCores nGpuArchCoresPerSM[] =
    { { 0x10,  8 },
      { 0x11,  8 },
      { 0x12,  8 },
      { 0x13,  8 },
      { 0x20, 32 },
      { 0x21, 48 },
      {   -1, -1 }
    };

    int index = 0;
    while (nGpuArchCoresPerSM[index].SM != -1) {
        if (nGpuArchCoresPerSM[index].SM == ((major << 4) + minor) ) {
            return nGpuArchCoresPerSM[index].Cores;
        }
        index++;
    }
    printf("MapSMtoCores undefined SMversion %d.%d!\n", major, minor);
    return -1;
}
// end of GPU Architecture definitions

int main(int argc, char ** argv)
{
    int rc = EXIT_SUCCESS;
    try {
        const int n = (argc > 1) ? atoi(argv[1]) : 100;
        const int stepSize = (argc > 2) ? atoi(argv[2]) : 175;

        // Prepare data.
        Matrix mxA(n);
        Matrix mxB(n);
        Matrix mxST(n, false);
        Matrix mxMT(n, false);
        Matrix mxCUDA(n, false);
        const int numCpu = Thread::getCpuCount();
        printf("Number of processors:\t%d\n", numCpu);
        int iGPUcount;
        cuSafeCall("cuInit", ::cuInit(0));
        cudaSafeCall("cudaGetDeviceCount", ::cudaGetDeviceCount(&iGPUcount));
        size_t maxComputePerf = 0;
        if (iGPUcount < 1) {
    	    fprintf(stderr, "CUDA Error: No CUDA device availabe!\n");
    	    return EXIT_FAILURE;
        } else {
            printf("Number of CUDA devices:\t%d\n", iGPUcount);
            // List the CUDA devices.
            const float memMB = 1.0f / (1024.0f * 1024.0f);
            CUdevice dev;
            CUcontext ctx;
            int maxPerfDevice = 0;
            int bestSMarch    = 0;
            for (int i = 0; i < iGPUcount; ++i) {
                cudaDeviceProp props;
                cudaSafeCall("cudaGetDeviceProperties", ::cudaGetDeviceProperties(&props, i));
                cudaSafeCall("cudaSetDevice", ::cudaSetDevice(i));
                cudaSafeCall("cudaSetDevice", ::cudaSetDevice(i));
                printf("\tCUDA device #%d:\t%s\n", i, props.name);
                // Try to find the fastest device.
                printf("\t\t- compute capability:\t%d.%d\n", props.major, props.minor);
                printf("\t\t- multiprocessor count:\t%d\n",
                    props.multiProcessorCount);
                int smPerMultiproc;
                if (props.major == 9999 && props.minor == 9999) {
                    smPerMultiproc = 1;
                } else {
                    smPerMultiproc = convertSMVer2Cores(props.major, props.minor);
                }
                printf("\t\t- SM per multiproc:\t%d\n", smPerMultiproc);
                printf("\t\t- frequency:\t%1.0f MHz\n",
                    props.clockRate * 0.001f);
                size_t free, total;
                cuSafeCall("cuDeviceGet", ::cuDeviceGet(&dev, i));
                cuSafeCall("cuCtxCreate", ::cuCtxCreate(&ctx, 0, dev));
                cuSafeCall("cuMemGetInfo", ::cuMemGetInfo(&free, &total));
                cuSafeCall("cuCtxDetach", ::cuCtxDetach(ctx));
                printf("\t\t- total memory:\t%1.0f MB\n", total * memMB);
                printf("\t\t- free memory:\t%1.0f MB\n", free * memMB);
                const size_t computePerf = props.multiProcessorCount * smPerMultiproc * props.clockRate;
                if (computePerf > maxComputePerf) {
                    // If we find GPU with SM major > 2, search only these
                    if (bestSMarch > 2) {
                        // If our device==dest_SM_arch, choose this, or else pass
                        if (props.major == bestSMarch) {	
                            maxComputePerf = computePerf;
                            maxPerfDevice  = i;
                        }
                    } else {
                        maxComputePerf = computePerf;
                        maxPerfDevice  = i;
                    }
                }
#if 0
                printf("\t\t- max grid size:\t%d x %d x %d\n",
                    props.maxGridSize[0],
                    props.maxGridSize[1],
                    props.maxGridSize[2]);
                printf("\t\t- max threads dim:\t%d x %d x %d\n",
                    props.maxThreadsDim[0],
                    props.maxThreadsDim[1],
                    props.maxThreadsDim[2]);
                printf("\t\t- max threads per block:\t%d\n",
                    props.maxThreadsPerBlock);
#endif
            }
            printf("\tSelected fastest CUDA device:\t#%d\n", maxPerfDevice);
            cudaSafeCall("cudaSetDevice", ::cudaSetDevice(maxPerfDevice));
        }
        printf("\n");
        fflush(stdout);

#if 0
        //Print the matrices.
        float * ptr = mxA.data();
        for (int row = 0; row < n; ++row) {
            for (int col = 0; col < n; ++col) {
                printf("  %g", *ptr);
                ++ptr;
            }
            printf("\n");
        }
#endif

        // Run the tests.
        test("CUDA", mulCUDA, mxA.data(), mxB.data(), mxCUDA.data(), n, (maxComputePerf / 1000) * stepSize);
#if 1
        test("Multi-thread", mulMT, mxA.data(), mxB.data(), mxMT.data(), n, numCpu);
        test("Single-thread", mulMT, mxA.data(), mxB.data(), mxST.data(), n, 1);
#endif
        printf("\n");

        // Check the results.
        const float * dataST = mxST.data();
        const float * dataMT = mxMT.data();
        const float * dataCUDA = mxCUDA.data();
        const int max_errors = 8;
        int errors = 0;
        for (int row = 1; row <= n; ++row) {
            for (int col = 1; col <= n; ++col) {
                if (!testEqual(*dataST, *dataMT, "Multithreading", row, col)) {
                    if (++errors == max_errors) {
                        break;
                    }
                    rc = EXIT_FAILURE;
                }
                if (!testEqual(*dataST, *dataCUDA, "CUDA", row, col)) {
                    if (++errors == max_errors) {
                        break;
                    }
                    rc = EXIT_FAILURE;
                }
                ++dataST, ++dataMT, ++dataCUDA;
            }
            if (errors == max_errors) {
                fprintf(stderr, "... (too much errors - skipped)\n");
                break;
            }
        }
    } catch (const Exception & e) {
        fprintf(stderr, "Exception caught: ");
        e.log(stderr);
        fprintf(stderr, "\n");
        rc = EXIT_FAILURE;
    } catch (const std::exception & e) {
        fprintf(stderr, "Exception caught: %s!\n", e.what());
        rc = EXIT_FAILURE;
    } catch (...) {
        fprintf(stderr, "Unhandled exception caught!\n");
        rc = EXIT_FAILURE;
    }
    return rc;
}


Exception::Exception(const char * const message)
    : m_message(message)
{}

/* virtual */ Exception::~Exception() throw()
{}

void Exception::log(FILE * const logFile) const
{
    onLog(logFile);
}

/* virtual */ void Exception::onLog(FILE * const logFile) const
{
    fprintf(logFile, "%s", (m_message) ? m_message : "unknown exception");
}


/* static */ float * Matrix::createData(const int n, const bool init)
{
    float * data = new float[n*n];
    if (init) {
        float * current = data;
        for (int r = 0; r < n; ++r) {
            for (int c = 0; c < n; ++c) {
                *current = static_cast< float >(getRand());
                ++current;
            }
        }
    }
    return data;
}

/* static */ int Matrix::getRand()
{
    static const int RANGE = 0x7FFF;
    static const int RANGE_HALF = RANGE >> 1;
    // Number in the interval <-RAND_MAX/2;RAND_MAX/2>.
    return ((rand() & RANGE) - RANGE_HALF) >> 4;
}

Matrix::Matrix(const int n, const bool init)
    : m_size(n)
    , m_data(createData(n, init))
{}

Matrix::~Matrix()
{
    delete[] m_data;
}

int Matrix::size() const
{
    return m_size;
}

float * Matrix::data() const
{
    return m_data;
}



#include "MatrixCUDA.hpp"


///@todo Try to utilize thread shared memory for faster memory access.
//       (like it is done in the NVidia SDK example)


// Currently, CUDA requires all textures to be file-scoped.
texture<float, 2, cudaReadModeElementType> texA;
texture<float, 2, cudaReadModeElementType> texB;

// Access data using NVidia texture units for faster access to global memory.
///@todo Check using of 2-D textures for even faster access.
__global__ void mulCUDArun(
    float * const x,
    const int n,
    const int start, const int end,
    const int blockStart)
{
	int row = (blockIdx.x * blockDim.x) + threadIdx.x;
	int col = (blockIdx.y * blockDim.y) + threadIdx.y;
    if ((row < n) && (col < n)) {
        const size_t rowSize = row * n;
        float * const ptrX = x + rowSize + col;
        // Compute the result[row, col].
        float sum = (0 == start) ? 0.0f : *ptrX;
        for (int i = start; i < end; ++i) {
//            sum = tex2D(texA, 0, 1);
            sum += tex2D(texA, i, row) * tex2D(texB, col, i);
        }
        *ptrX = sum;
    }
}


class CudaException: public Exception {

public:

    CudaException(const char * const name, const cudaError_t error)
        : m_name(name)
        , m_error(error)
    {}

protected:

    virtual void onLog(FILE * const logFile) const
    {
        fprintf(logFile, "%s() failed with error %d:\t%s",
            m_name, m_error, ::cudaGetErrorString(m_error));
    }

private:

    const char * const m_name;
    const cudaError_t m_error;

};

class CuException: public Exception {

public:

    CuException(const char * const name, const CUresult error)
        : m_name(name)
        , m_error(error)
    {}

protected:

    virtual void onLog(FILE * const logFile) const
    {
        fprintf(logFile, "%s() failed with error %d",
            m_name, m_error /*, ::cuGetErrorString(m_error)*/);
    }

private:

    const char * const m_name;
    const CUresult m_error;

};

void cudaSafeCall(const char * const funcName, const cudaError_t error) {
    if (::cudaSuccess != error) {
        throw CudaException(funcName, error);
	}
}

void cuSafeCall(const char * const funcName, const CUresult error) {
    if (::CUDA_SUCCESS != error) {
        throw CuException(funcName, error);
	}
}

class CudaMemory {

public:

    CudaMemory(void * data = NULL)
        : m_data(data)
    {}

    ~CudaMemory()
    {
        clear();
    }

public:

    CudaMemory & operator = (cudaArray * data)
    {
        if (data != m_data) {
            clear();
            m_data = data;
        }
        return *this;
    }

private:

    void clear()
    {
        if (m_data) {
            ::cudaFree(m_data);
        }
    }

private:

    void * m_data;

}; // class CudaMemory


class CudaMemoryArray {

public:

    CudaMemoryArray(cudaArray * data = NULL)
        : m_data(data)
    {}

    ~CudaMemoryArray()
    {
        clear();
    }

public:

    CudaMemoryArray & operator = (cudaArray * data)
    {
        if (data != m_data) {
            clear();
            m_data = data;
        }
        return *this;
    }

private:

    void clear()
    {
        if (m_data) {
            ::cudaFreeArray(m_data);
        }
    }

private:

    cudaArray * m_data;

}; // class CudaMemoryArray


void mulCUDA(
    const float * const a,
    const float * const b,
    float * const x,
    const int n,
    const size_t maxComputePerf)
{
    // Prepare the data for CUDA.
    // It is done in advance, to not include this preparation in the time
    // measurement.
    cudaArray * gpuInput[2];
    float * gpuOutput;
    const size_t memSize = n * sizeof(float);
    const size_t fullSize = n * memSize;
    cudaChannelFormatDesc floatTex = ::cudaCreateChannelDesc<float>();
    CudaMemoryArray guardsInput[2];
    for (int i = 0; i < 2; ++i) {
        cudaSafeCall("cudaMallocArray",
            ::cudaMallocArray(gpuInput + i, &floatTex, n, n));
        guardsInput[i] = gpuInput[i];
    }
    cudaSafeCall("cudaMalloc",
        ::cudaMalloc(reinterpret_cast< void ** >(&gpuOutput), fullSize));
    CudaMemory guardOutput(gpuOutput);
    // Copy the source data to device,
    cudaSafeCall("cudaMemcpy2DToArray", ::cudaMemcpy2DToArray(gpuInput[0], 0, 0,
        a, memSize, memSize, n, cudaMemcpyHostToDevice));
    cudaSafeCall("cudaMemcpy2DToArray", ::cudaMemcpy2DToArray(gpuInput[1], 0, 0,
        b, memSize, memSize, n, cudaMemcpyHostToDevice));
    // Setup the texture system.
    cudaSafeCall("cudaBindTextureToArray", ::cudaBindTextureToArray(texA, gpuInput[0], floatTex));
    cudaSafeCall("cudaBindTextureToArray", ::cudaBindTextureToArray(texB, gpuInput[1], floatTex));//
    // Compute the multiplication of matrices.
    int devId;
    cudaSafeCall("cudaGetDevice", ::cudaGetDevice(&devId));
    cudaDeviceProp props;
    cudaSafeCall("cudaGetDeviceProperties", ::cudaGetDeviceProperties(&props, devId));
    // Determine the maximum thread dimensions.
    int threadDim[2] = {1, 1};
    int threads = 1;
    int dimId = 0;
    while (threadDim[1] < n) {
        threads <<= 1;
        if (threads > props.maxThreadsPerBlock) {
            break;
        }
        threadDim[dimId] <<= 1;
        dimId = ~dimId & 1;
    }
	dim3 dimBlock(threadDim[0], threadDim[1]);
    int blockDim[2] = {1, 1};
    for (dimId = 0; dimId < 2; ++dimId) {
        int threads = threadDim[dimId];
        while (threads < n) {
            threads <<= 1;
            blockDim[dimId] <<= 1;
            if (blockDim[dimId] > props.maxThreadsDim[dimId]) {
                throw Exception("Matrix too big.");
            }
        }
    }
	dim3 dimGrid(blockDim[0], blockDim[1]);
    ///@todo Base the estimation on the GPU props.
    const int nMax = static_cast< size_t >(max(maxComputePerf * 1000.0 / n / n, 1.0));
    //Run the calculation.
    int done = 0;
    // Flush all operations.
    cudaSafeCall("cudaThreadSynchronize", ::cudaThreadSynchronize());
    while (done < n) {
        const int end = min(done + nMax, n);
        mulCUDArun<<<dimGrid, dimBlock>>>(gpuOutput, n, done, end, done * n);
        done = end;
        // Wait for all threads to complete.
	    cudaSafeCall("cudaThreadSynchronize", ::cudaThreadSynchronize());
    }
    // Get the result data from device.
    cudaSafeCall("cudaMemcpy", ::cudaMemcpy(x, gpuOutput, fullSize, cudaMemcpyDeviceToHost));
    // Flush all operations.
    cudaSafeCall("cudaThreadSynchronize", ::cudaThreadSynchronize());
    // Unbind textures.
    cudaSafeCall("cudaUnbindTexture", ::cudaUnbindTexture(texA));
    cudaSafeCall("cudaUnbindTexture", ::cudaUnbindTexture(texB));
}

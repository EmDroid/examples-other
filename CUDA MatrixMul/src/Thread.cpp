
#include <cstdlib>
#include <cassert>


#include "Matrix.hpp"

#include "Thread.hpp"


typedef int (Thread::* ThreadProc)();


#if defined(_WIN32)
// Windows version.

#define WIN32_LEAN_AND_MEAN
#include <windows.h>


class ApiException: public Exception {

public:

	ApiException(const char * const name, const DWORD code = ::GetLastError())
        : m_name(name)
        , m_code(code)
        , m_message(createMessage(code))
    {}

    virtual ~ApiException()
    {
        ::LocalFree(m_message);
    }

protected:

    virtual void onLog(FILE * const logFile) const
    {
        fprintf(logFile, "%s() failed with error %d:\t%s",
            m_name, m_code, m_message);
    }

private:

    LPTSTR createMessage(const DWORD code)
    {
        LPTSTR msg;
        ::FormatMessage(
            FORMAT_MESSAGE_FROM_SYSTEM |
            FORMAT_MESSAGE_ALLOCATE_BUFFER |
            FORMAT_MESSAGE_IGNORE_INSERTS,
            NULL,
            code,
            MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
            reinterpret_cast< LPTSTR >(&msg),
            0,
            NULL);
        return msg;
    }

private:

    const char * const m_name;
    const DWORD m_code;
    char * const m_message;

}; // class ApiException


class ThreadImpl {

public:

    static int getCpuCount()
    {
        SYSTEM_INFO sysInfo;
        ::SetLastError(ERROR_SUCCESS);
        ::GetSystemInfo(&sysInfo);
        // not able to detect an error - ?
        const DWORD code = ::GetLastError();
        if (code) {
            throw ApiException("GetSystemInfo", code);
        }
        return sysInfo.dwNumberOfProcessors;
    }

private:

    static DWORD WINAPI threadProc(LPVOID thread)
    {
        assert(thread != NULL);
        return static_cast< Thread * >(thread)->runThreadProc();
    }

public:

    // Create the suspended thread.
    ThreadImpl(Thread * const thread)
        : m_handle(NULL)
        , m_thread(thread)
    {
        assert(thread != NULL);
    }

    ~ThreadImpl()
    {
        // Close the thread handle.
        if (m_handle) {
            ::CloseHandle(m_handle);
            // Errors not checked.
        }
    }

public:

    void start()
    {
        assert(m_handle == NULL);
        DWORD id;
        m_handle = ::CreateThread(NULL, 0, threadProc, m_thread,
            0, &id);
        if (!m_handle) {
            throw ApiException("CreateThread");
        }
    }

    bool join()
    {
        assert(m_handle != NULL);
        // Wait for the thread to terminate.
        const DWORD waitStatus = ::WaitForSingleObject(m_handle, INFINITE);

        switch (waitStatus) {
        case WAIT_FAILED:
            throw ApiException("WaitForSingleObject");
        case WAIT_OBJECT_0:
            // Thread did exit successfully.
            return true;
        case WAIT_TIMEOUT:
            // Thread did not exit nor any message came in the specified timeout.
            // Fall through.
        default:
            // Some other condition - handle as failure.
            return false;
        }
    }

private:

    HANDLE m_handle;

    Thread * const m_thread;

}; // class ThreadImpl


#elif defined (unix)
// UNIX version.

#include <cerrno>
#include <cstring>
#include <pthread.h>

#include <string>
using std::string;
// using std::npos;

#include <fstream>
using std::ifstream;


class ApiException: public Exception {

public:

	ApiException(const char * const name, const int code = errno)
        : m_name(name)
        , m_code(code)
    {}

protected:

    virtual void onLog(FILE * const logFile) const
    {
        fprintf(logFile, "%s() failed with error %d:\t%s",
            m_name, m_code, strerror(m_code));
    }

private:

    const char * const m_name;
    const int m_code;

}; // class ApiException


class ThreadImpl {

public:

    static int getCpuCount()
    {
#ifdef _SC_NPROCESSORS_ONLN
        // this works for Solaris and Linux 2.6
        errno = 0;
        int cpuCount = static_cast< int >(sysconf(_SC_NPROCESSORS_ONLN));
        if (EINVAL != cpuCount) {
            return cpuCount;
        }
        // continue
#endif // _SC_NPROCESSORS_ONLN

#ifdef __linux__
        // read from /proc/cpuinfo

        ifstream cpuInfoFile("/proc/cpuinfo");
        if (cpuInfoFile.is_open()) {
            // Load the whole file.
            int cpuCount = 0;
            string line;
            while (!cpuInfoFile.eof()) {
                cpuInfoFile >> line;
                if (!line.compare("processor")) {
                    ++cpuCount;
                }
            }
            if (cpuCount > 0) {
                return cpuCount;
            }
        }
#endif // __linux__

        // unknown
        return 1;
    }

private:

    static void * threadProc(void * thread)
    {
        assert(thread != NULL);
        return reinterpret_cast< void * >(
            static_cast< Thread * >(thread)->runThreadProc());
    }

public:

    // Create the suspended thread.
    ThreadImpl(Thread * const thread)
        : m_handle(0)
        , m_thread(thread)
    {
        assert(thread != NULL);
    }

    /*
    ~ThreadImpl()
    {
        // Handle closed automatically.
    }
    */

public:

    void start()
    {
        assert(m_handle == 0);
        if (::pthread_create(&m_handle, NULL, threadProc, m_thread)) {
            throw ApiException("pthread_create");
        }
    }

    bool join()
    {
        assert(m_handle != 0);
        // Wait for the thread to terminate.
        if (::pthread_join(m_handle, NULL)) {
            throw ApiException("pthread_join");
        }
        return true;
    }

private:

    pthread_t m_handle;

    Thread * const m_thread;

}; // class ThreadImpl


#else

#error Unknown platform!

#endif


ThreadVector::ThreadVector(const int size)
    : vector< Thread * >(size)
{}

ThreadVector::~ThreadVector()
{
    // Automatically free allocated threads.
    for (const_iterator iter = begin(); iter < end(); ++iter) {
        delete *iter;
    }
}


/* static */ int Thread::getCpuCount()
{
    // Check only once and cache the value
    // (the count of CPUs in the system is expectend to not change very often).
    static int cpuCount = ThreadImpl::getCpuCount();
    return cpuCount;
}

/* static */ bool Thread::join(const ThreadVector & threads)
{
    bool status = true;
    for (ThreadVector::const_iterator iter = threads.begin(); iter < threads.end(); ++iter) {
        if (!(*iter)->join()) {
            status = false;
        }
    }
    return status;
}

Thread::Thread()
    : m_impl(createImpl())
    , m_started(false)
{}

/* virtual */ Thread::~Thread()
{
    delete m_impl;
}

void Thread::start()
{
    if (m_started) {
        throw Exception("Thread already started!");
    }
    m_impl->start();
    m_started = true;
}

bool Thread::join()
{
    if (!m_started) {
        throw Exception("Thread not started!");
    }
    const bool status = m_impl->join();
    if (status) {
        m_started = false;
    }
    return status;
}

ThreadImpl * Thread::createImpl()
{
    return new ThreadImpl(this);
}

int Thread::runThreadProc()
{
    try {
        onRun();
    } catch (...) {
        // No further processing so far.
        return EXIT_FAILURE;
    }
    return EXIT_SUCCESS;
}

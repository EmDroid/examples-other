

#ifndef __THREAD_HPP_INCLUDE_BLOCK__
#define __THREAD_HPP_INCLUDE_BLOCK__


#include <vector>
using std::vector;


class Thread;

class ThreadVector: public vector< Thread * > {

public:

    ThreadVector(const int size);

    ~ThreadVector();

}; // class ThreadVector


class ThreadImpl;

class Thread {

public:

    static int getCpuCount();

    static bool join(const ThreadVector & threads);

public:

    Thread();

    virtual ~Thread();

public:

    void start();

    bool join();

protected:

    virtual void onRun() = 0;

private:

    ThreadImpl * createImpl();

    int runThreadProc();

    friend class ThreadImpl;

private:

    ThreadImpl * const m_impl;

    bool m_started;

}; // class Thread


#endif // __THREAD_HPP_INCLUDE_BLOCK__

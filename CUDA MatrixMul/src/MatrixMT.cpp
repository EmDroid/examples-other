
#include <cassert>

#include <vector>
using std::vector;


#include "Math.hpp"
#include "Thread.hpp"

#include "MatrixMT.hpp"


class MatrixMulThread: public Thread {

public:

    MatrixMulThread(
            const float * const a,
            const float * const b,
            float * const x,
            const int size,
            const int rowStart, const int rowEnd)
        : m_a(a)
        , m_b(b)
        , m_x(x)
        , m_size(size)
        , m_rowStart(rowStart)
        , m_rowEnd(rowEnd)
    {
        // Auto-start.
        start();
    }

public:

    static void compute(
        const float * const a,
        const float * const b,
        float * const x,
        const int n,
        const int rowStart, const int rowEnd)
    {
        float * ptrX = x;
        const float * ptrA = a;
        for (int row = rowStart; row < rowEnd; ++row) {
            const float * ptrB = b;
            for (int col = 0; col < n; ++col) {
                // Compute the result[row, col].
                *ptrX = 0;
                const float * ptrAx = ptrA;
                const float * ptrBx = ptrB;
                for (int i = 0; i < n; ++i) {
                    assert(ptrAx == (a + (row - rowStart) * n + i));
                    assert(ptrBx == (b + i * n + col));
                    *ptrX += (*ptrAx) * (*ptrBx);
                    ++ptrAx;
                    ptrBx += n;
                }
                ++ptrX;
                ++ptrB;
            }
            ptrA += n;
        }
    }

protected:

    virtual void onRun()
    {
        compute(m_a, m_b, m_x,
            m_size, m_rowStart, m_rowEnd);
    }

private:

    const float * const m_a;
    const float * const m_b;
    float * const m_x;

    const int m_size;
    const int m_rowStart;
    const int m_rowEnd;

}; // class Thread


void mulMT(
    const float * const a,
    const float * const b,
    float * const x,
    const int n,
    const size_t threads)
{
    // Compute the multiplication of matrices.
    if (threads == 1) {
        // Run single thread in-place.
        MatrixMulThread::compute(a, b, x,
            n, 0, n);
    } else {
        // Run in threads.
        const int threadRows = n / threads;
        // If threads count greater than n, we no not need so much threads.
        const int threadsCount = n / threadRows;
        ThreadVector threads(threadsCount);
        const float * ptrA = a;
        float * ptrX = x;
        int row = 0;
        const int rowsInc = threadRows * n;
        for (ThreadVector::iterator iter = threads.begin(); iter < threads.end(); ++iter) {
            const int rowEnd = std::min(row + threadRows, n);
            *iter = new MatrixMulThread(ptrA, b, ptrX, n, row, rowEnd);
//            (*iter)->join(); // uncomment to debug
            row = rowEnd;
            // no need to check for last possibly smaller number of rows - will quit anyway
            ptrA += rowsInc;
            ptrX += rowsInc;
        }
        Thread::join(threads);
    }
}

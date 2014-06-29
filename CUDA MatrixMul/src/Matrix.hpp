

#ifndef __MATRIX_HPP_INCLUDE_BLOCK__
#define __MATRIX_HPP_INCLUDE_BLOCK__

#include <cstdio>
#include <exception>


class Matrix {

private:

    static float * createData(const int n, const bool init);

    static int getRand();

public:

    Matrix(const int n, const bool init = true);

    ~Matrix();

public:

    int size() const;

    float * data() const;

private:

    const int m_size;
    float * const m_data;

}; // class Matrix


class Exception: public std::exception {

public:

    Exception(const char * const message = NULL);

    virtual ~Exception() throw();

public:

    void log(FILE * const logFile = stderr) const;

protected:

    virtual void onLog(FILE * const logFile) const;

private:

    const char * const m_message;

}; // class Exception


#endif // __MATRIX_HPP_INCLUDE_BLOCK__

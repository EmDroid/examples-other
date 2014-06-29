

#ifndef __MATRIX_MT_HPP_INCLUDE_BLOCK__
#define __MATRIX_MT_HPP_INCLUDE_BLOCK__


#include "Matrix.hpp"


void mulMT(
    const float * const a,
    const float * const b,
    float * const x,
    const int n,
    const size_t threads);


#endif // __MATRIX_MT_HPP_INCLUDE_BLOCK__

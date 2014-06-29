
#ifndef __MATRIX_CUDA_HPP_INCLUDE_BLOCK__
#define __MATRIX_CUDA_HPP_INCLUDE_BLOCK__

#include <cuda.h>
#include <cuda_runtime_api.h>


#include "Matrix.hpp"


enum {
    CUDA_ALGO_SLOW = 0,
    CUDA_ALGO_TEXTURE_1D,
    CUDA_ALGO_TEXTURE_2D,
    CUDA_ALGO_COUNT
};

void cudaSafeCall(const char * const funcName, cudaError_t error);

void cuSafeCall(const char * const funcName, CUresult error);

void mulCUDA(
    const float * const a,
    const float * const b,
    float * const x,
    const int n,
    const size_t maxComputePerf);


#endif // __MATRIX_CUDA_HPP_INCLUDE_BLOCK__

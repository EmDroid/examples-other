

#ifndef __MATH_HPP_INCLUDE_BLOCK__
#define __MATH_HPP_INCLUDE_BLOCK__

#include <cmath>

template< class T >
bool epsilonEquals(const T & a, const T & b)
{
    // cca 1e-9, but in binary (for faster multiplication).
    static const T epsilonNorm = 1.0 / (1 << 30);
    if (0 == a) {
        return std::abs(b) < epsilonNorm;
    }
    if (0 == b) {
        return std::abs(a) < epsilonNorm;
    }
    const T epsilon = std::abs(a) * epsilonNorm;
    return std::abs(a - b) < epsilon;
}


#endif // __MATH_HPP_INCLUDE_BLOCK__

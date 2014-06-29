Minimal NVidia Driver version:	185.85


Command line parameters:


Matrix [size [step]]

	size ... the matrix size (e.g. 1000 for 1000x1000 square matrices)
	step ... the kernel execution step

The step is 175 by default. The higher, the better (more operations will run at once);
however, when set too high, the kernel can reach the execution maximum time and be terminated.
So, if you experience kernel timeouts, insert lower value on the command line.

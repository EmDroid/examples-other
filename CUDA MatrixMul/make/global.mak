
PROJECT = Matrix

TARGET = $(PROJECT)$(EXE_SFX)

OBJS = \
$(PROJECT).$(OBJ_SFX) \
$(PROJECT)MT.$(OBJ_SFX) \
Thread.$(OBJ_SFX)

NVOBJS = \
$(PROJECT)CUDA.$(OBJ_SFX)

NVCC = nvcc
NVLIBS = $(LD_LIB_PFX)cuda$(LD_LIB_SFX) $(LD_LIB_PFX)cudart$(LD_LIB_SFX)

SRC_DIR = ..$(PATH_SEP)..$(PATH_SEP)..$(PATH_SEP)src$(PATH_SEP)

# Make all.
all:    $(TARGET)


# Clean.
clean:
	-@$(RM) $(OBJS) $(NVOBJS) $(PROJECT)CUDA.linkinfo $(NOERR)

# Clean all.
cleanall clean_all:	clean
	-@$(RM) $(TARGET) $(NOERR)


$(TARGET):	$(OBJS) $(NVOBJS)
	$(LD) $(LDFLAGS) $(LD_OUT)$@ $(NVLIBS) $(LIBS) $(OBJS) $(NVOBJS)

$(PROJECT).$(OBJ_SFX):	$(SRC_DIR)$(PROJECT).cpp

$(PROJECT)MT.$(OBJ_SFX):	$(SRC_DIR)$(PROJECT)MT.cpp

$(PROJECT)CUDA.$(OBJ_SFX):	$(SRC_DIR)$(PROJECT)CUDA.cu

$(PROJECT).$(OBJ_SFX):	$(SRC_DIR)$(PROJECT).cpp

Thread.$(OBJ_SFX):	$(SRC_DIR)Thread.cpp

$(OBJS):
	$(CPP) $(CPP_COMPILE) $(CPPFLAGS) $(CPP_OUT)$@ $<

$(NVOBJS):
	$(NVCC) -c $(NVFLAGS) -o $@ $<

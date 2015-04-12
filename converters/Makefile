all: convert conv_new trs_conv

convert: convert.o string_utils.o
	gcc -o convert convert.o string_utils.o

conv_new: conv_new.o string_utils.o
	gcc -o conv_new conv_new.o string_utils.o

trs_conv: trs_conv.c
	gcc -o trs_conv trs_conv.c

clean:
	rm -f *~ *.o convert conv_new trs_conv

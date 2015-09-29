all: convert convert_interim trs_conv

convert: convert.o string_utils.o
	gcc -o convert convert.o string_utils.o

convert_interim: convert_interim.o string_utils.o
	gcc -o convert_interim convert_interim.o string_utils.o

trs_conv: trs_conv.c
	gcc -o trs_conv trs_conv.c

clean:
	rm -f *~ *.o convert convert_interim trs_conv

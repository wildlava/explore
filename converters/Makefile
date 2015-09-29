all: convert convert_interim

convert: convert.o string_utils.o
	gcc -o convert convert.o string_utils.o

convert_interim: convert_interim.o string_utils.o
	gcc -o convert_interim convert_interim.o string_utils.o

clean:
	rm -f *~ *.o convert convert_interim

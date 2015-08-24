#ifndef BITARRAY_H
#define BITARRAY_H

/* These work best as macros, since GCC doesn't know how to optimize static
 * inline functions very well. */
#define bit_is_set(mess, bit) (mess[bit / 8] & (1 << (bit % 8)))
#define bit_set(mess, bit) (mess[bit / 8] |= (1 << (bit % 8)))

#endif

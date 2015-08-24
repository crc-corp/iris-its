/*
 * clump -- Collection Library Utilising Memory Pools
 * Copyright (C) 2007-2008  Douglas P Lau
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
#include <stdlib.h>
#include "clump.h"

#define MALLOC_SIZE	(4096)

/** Allocate a new memory pool.
 *
 * This method will allocate a new cl_pool structure on the heap. The new
 * structure will not be initialized. Structures allocated with the method must
 * be released with the cl_pool_delete method. This is just a convenience
 * function for Python ctypes integration.
 */
struct cl_pool *cl_pool_new() {
	return malloc(sizeof(struct cl_pool));
}

/** Delete a memory pool.
 *
 * Call this method to delete a memory pool previously allocated with
 * cl_pool_new. This is just a convenience function for Python ctypes
 * integration.
 */
void cl_pool_delete(struct cl_pool *p) {
	free(p);
}

/** Get the slots per block.
 *
 * Calculate the number of slots per block in the memory pool.
 */
static inline unsigned int cl_pool_block_slots(struct cl_pool *p) {
	return (MALLOC_SIZE - sizeof(void *)) / p->n_bytes;
}

/** Initialize a memory pool block.
 *
 * Each slot in a memory pool contains a freelist pointer to the next
 * free slot. The first pointer in the memory pool points to the next
 * allocated block in the memory pool.
 *
 *	(0) -> (next block)		sizeof(void *)
 *	(1) -> (2)			pool.n_bytes
 *	(2) -> (3)			pool.n_bytes
 *	(3) -> (4)			pool.n_bytes
 */
static void **cl_pool_init_block(struct cl_pool *p, void **block) {
	void **slot = block + 1;
	char *next = (char *)slot;
	char *last = next + cl_pool_block_slots(p) * p->n_bytes;
	for(next += p->n_bytes; next < last; next += p->n_bytes) {
		*slot = next;
		slot = (void **)next;
	}
	return slot;
}

/** Add a block to the memory pool.
 *
 * When the freelist is empty, this function must be called. It allocates a
 * new block for the memory pool and initializes the freelist.
 */
static void *cl_pool_add_block(struct cl_pool *p) {
	void **block, **last;

	block = malloc(MALLOC_SIZE);
	if(block == NULL)
		return NULL;
	*block = p->block;	/* link to previous block */
	p->block = block;	/* update block head */
	p->head = block + 1;	/* update slot head */
	last = cl_pool_init_block(p, block);
	*last = NULL;

	return block;
}

/** Initialize a memory pool.
 *
 * A memory pool must be initialized before it can be used. Memory pools are
 * designed for allocating many small objects of the same size.
 */
struct cl_pool *cl_pool_init(struct cl_pool *p, unsigned int s) {
	p->n_bytes = s;
	p->block = NULL;
	p->head = NULL;
	return p;
}

/** Allocate an object from the memory pool.
 *
 * Call this function to allocate a new object from the memory pool.
 */
void *cl_pool_alloc(struct cl_pool *p) {
	void **slot;
	if(p->head == NULL) {
		if(cl_pool_add_block(p) == NULL)
			return NULL;
	}
	slot = p->head;
	p->head = *slot;
	return slot;
}

/** Release an object back to the memory pool.
 *
 * Call the function when done using an object to release it back into the
 * memory pool.
 */
void cl_pool_release(struct cl_pool *p, void *m) {
	void **slot = m;

	*slot = p->head;
	p->head = m;
}

/** Release all allocated objects back to the memory pool.
 *
 * Call this function to release all previously allocated objects back to the
 * memory pool.
 */
void cl_pool_release_all(struct cl_pool *p) {
	void **block = p->block;
	void *next = NULL;

	while(block) {
		void **slot = cl_pool_init_block(p, block);
		*slot = next;
		next = block + 1;
		block = *block;
	}
	p->head = next;
}

/** Destroy a memory pool.
 *
 * This function destroys a memory pool. Use it when the memory pool is no
 * longer needed.
 */
void cl_pool_destroy(struct cl_pool *p) {
	void **block = p->block;

	while(block) {
		void *b = block;
		block = *block;
		free(b);
	}
	p->block = NULL;
	p->head = NULL;
}

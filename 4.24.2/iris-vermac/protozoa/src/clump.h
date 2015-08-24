#ifndef CLUMP_H
#define CLUMP_H

typedef void (CL_CALLBACK) (void *value, void *bound);

typedef enum cl_compare_t {
	CL_LESS = -1,
	CL_EQUAL = 0,
	CL_GREATER = 1
} cl_compare_t;

typedef enum cl_dup_t {
	CL_DUP_REJECT,		/*< reject duplicate items in a collection */
	CL_DUP_ALLOW,		/*< allow duplicate items in a collection */
	CL_DUP_REPLACE		/*< replace duplicate items in a collection */
} cl_dup_t;

/** Memory pool allocator.
 *
 * A memory pool is a special type of memory allocator. It is designed for
 * efficiently allocating many small objects of the same size.
 */
struct cl_pool {
	unsigned int	n_bytes;	/* number of bytes for each object */
	void		*block;		/* first malloc'd block */
	void		*head;		/* head of free list */
};

struct cl_pool *cl_pool_new();
void cl_pool_delete(struct cl_pool *p);
struct cl_pool *cl_pool_init(struct cl_pool *p, unsigned int s);
void cl_pool_destroy(struct cl_pool *p);
void *cl_pool_alloc(struct cl_pool *p);
void cl_pool_release(struct cl_pool *p, void *m);
void cl_pool_release_all(struct cl_pool *p);

/** Red-black tree.
 *
 * A red-black tree is a sorted collection. Iterating over the items is a
 * fast operation.
 */
struct cl_rbtree {
	struct cl_pool		pool;
	struct cl_rbnode	*root;
	unsigned int		n_items;
	void * (*add) (struct cl_rbtree *tree, void *value);
	cl_compare_t (*compare)(const void *value0, const void *value1);
};

struct cl_rbtree *cl_rbtree_new();
void cl_rbtree_delete(struct cl_rbtree *tree);
struct cl_rbtree *cl_rbtree_init(struct cl_rbtree *tree, cl_dup_t dup,
	cl_compare_t (*compare)(const void *value0, const void *value1));
unsigned int cl_rbtree_count(struct cl_rbtree *tree);
void *cl_rbtree_add(struct cl_rbtree *tree, void *value);
void *cl_rbtree_get(struct cl_rbtree *tree, void *value);
void *cl_rbtree_peek(struct cl_rbtree *tree);
void *cl_rbtree_remove(struct cl_rbtree *tree, void *value);
void cl_rbtree_for_each(struct cl_rbtree *tree, CL_CALLBACK *call, void *bound);
void cl_rbtree_clear(struct cl_rbtree *tree, CL_CALLBACK *call, void *bound);

#endif

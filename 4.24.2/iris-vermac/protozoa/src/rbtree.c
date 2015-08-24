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
#include <stdbool.h>
#include <stdlib.h>
#include "clump.h"

typedef void* (* PCL_ADD) (struct cl_rbtree *tree, void *value);
static PCL_ADD cl_dup_add_method(cl_dup_t dup);

typedef enum cl_direction_t {
	LEFT = 0,
	RIGHT = 1
} cl_direction_t;

static inline cl_direction_t cl_direction(bool b) {
	if(b)
		return RIGHT;
	else
		return LEFT;
}

typedef enum cl_rbcolor_t {
	CL_BLACK = 0,		/*< color enum for black nodes */
	CL_RED = 1		/*< color enum for red nodes */
} cl_rbcolor_t;

struct cl_rbnode {
	enum cl_rbcolor_t	color;		/* CL_RED or CL_BLACK */
	struct cl_rbnode	*link[2];	/* LEFT and RIGHT links */
	void			*value;		/* user-defined value */
};

static void cl_rbnode_init(struct cl_rbnode *n, void *value) {
	n->color = CL_RED;
	n->link[LEFT] = NULL;
	n->link[RIGHT] = NULL;
	n->value = value;
}

static inline bool cl_rbnode_is_red(struct cl_rbnode *n) {
	return n != NULL && n->color == CL_RED;
}

static inline bool cl_rbnode_is_not_red(struct cl_rbnode *n) {
	return n == NULL || n->color != CL_RED;
}

static struct cl_rbnode *cl_rbnode_rotate_single(struct cl_rbnode *n,
	 cl_direction_t dir)
{
	struct cl_rbnode *m = n->link[!dir];

	n->link[!dir] = m->link[dir];
	m->link[dir] = n;

	n->color = CL_RED;
	m->color = CL_BLACK;

	return m;
}

static struct cl_rbnode *cl_rbnode_rotate_double(struct cl_rbnode *n,
	cl_direction_t dir)
{
	n->link[!dir] = cl_rbnode_rotate_single(n->link[!dir], !dir);
	return cl_rbnode_rotate_single(n, dir);
}

/** Initialize a red-black tree.
 *
 * A red-black tree must be initialized before it can be used.
 */
struct cl_rbtree* cl_rbtree_init(struct cl_rbtree *tree, cl_dup_t dup,
	cl_compare_t (*compare)(const void *value0, const void *value1))
{
	if(cl_pool_init(&tree->pool, sizeof(struct cl_rbnode)) == NULL)
		return NULL;
	tree->root = NULL;
	tree->n_items = 0;
	tree->add = cl_dup_add_method(dup);
	if(tree->add == NULL)
		return NULL;
	tree->compare = compare;
	return tree;
}

/** Get the count of items.
 *
 * This method will return the count of items currently in the tree.
 */
unsigned int cl_rbtree_count(struct cl_rbtree *tree) {
	return tree->n_items;
}

static void cl_rbtree_insert2(struct cl_rbtree *tree, struct cl_rbnode *n) {
	struct cl_rbnode froot = {0};		/* fake root */
	struct cl_rbnode *t = &froot;		/* great grantparent */
	struct cl_rbnode *g = NULL;		/* grandparent */
	struct cl_rbnode *p = NULL;		/* parent */
	struct cl_rbnode *q = tree->root;	/* iterator */
	cl_direction_t dir = LEFT;
	cl_direction_t last = LEFT;

	froot.link[RIGHT] = tree->root;

	while(1) {
		/* have we reached a leaf node? */
		if(q == NULL) {
			q = n;
			p->link[dir] = q;
		}
		else if(cl_rbnode_is_red(q->link[LEFT]) &&
			cl_rbnode_is_red(q->link[RIGHT]))
		{
			q->color = CL_RED;
			q->link[LEFT]->color = CL_BLACK;
			q->link[RIGHT]->color = CL_BLACK;
		}
		/* check for a red violation */
		if(cl_rbnode_is_red(q) && cl_rbnode_is_red(p)) {
			cl_direction_t d = cl_direction(t->link[RIGHT] == g);
			if(q == p->link[last])
				t->link[d] = cl_rbnode_rotate_single(g, !last);
			else
				t->link[d] = cl_rbnode_rotate_double(g, !last);
		}
		if(q == n)
			break;
		last = dir;
		dir = cl_direction(
			tree->compare(q->value, n->value) == CL_LESS
		);
		if(g != NULL)
			t = g;		/* update great grandparent */
		g = p;			/* update grandparent */
		p = q;			/* update parent */
		q = q->link[dir];	/* update iterator */
	}

	tree->root = froot.link[RIGHT];
}

static void cl_rbtree_insert(struct cl_rbtree *tree, struct cl_rbnode *n) {
	if(tree->root == NULL)
		tree->root = n;
	else
		cl_rbtree_insert2(tree, n);
	tree->root->color = CL_BLACK;
}

static void *cl_rbtree_add_dup_allow(struct cl_rbtree *tree, void *value) {
	struct cl_rbnode *n = cl_pool_alloc(&tree->pool);
	if(n == NULL)
		return NULL;

	cl_rbnode_init(n, value);
	cl_rbtree_insert(tree, n);
	tree->n_items++;
	return value;
}

static struct cl_rbnode *cl_rbtree_lookup(struct cl_rbtree *tree, void *value) {
	struct cl_rbnode* n = tree->root;
	if(n != NULL) {
		cl_compare_t c = tree->compare(n->value, value);
		while(c != CL_EQUAL) {
			if(c == CL_GREATER)
				n = n->link[LEFT];
			else
				n = n->link[RIGHT];
			if(n == NULL)
				break;
			c = tree->compare(n->value, value);
		}
	}
	return n;
}

static void *cl_rbtree_add_dup_reject(struct cl_rbtree *tree, void *value) {
	struct cl_rbnode *n = cl_rbtree_lookup(tree, value);
	if(n != NULL)
		return n->value;
	else
		return cl_rbtree_add_dup_allow(tree, value);
}

static void *cl_rbtree_add_dup_replace(struct cl_rbtree *tree, void *value) {
	struct cl_rbnode *n = cl_rbtree_lookup(tree, value);
	if(n != NULL) {
		void *v = n->value;
		n->value = value;
		return v;
	} else
		return cl_rbtree_add_dup_allow(tree, value);
}

static PCL_ADD cl_dup_add_method(cl_dup_t dup) {
	switch(dup) {
		case CL_DUP_REJECT:
			return cl_rbtree_add_dup_reject;
		case CL_DUP_ALLOW:
			return cl_rbtree_add_dup_allow;
		case CL_DUP_REPLACE:
			return cl_rbtree_add_dup_replace;
		default:
			return NULL;
	}
}

/** Add an item into the tree.
 *
 * This method will add a new item to the red-black tree.
 */
void *cl_rbtree_add(struct cl_rbtree *tree, void *value) {
	return tree->add(tree, value);
}

/** Get an item from the tree.
 *
 * This method will get a matching item from the red-black tree.
 */
void *cl_rbtree_get(struct cl_rbtree *tree, void *value) {
	struct cl_rbnode* n = cl_rbtree_lookup(tree, value);
	if(n == NULL)
		return NULL;
	else
		return n->value;
}

static inline void cl_rbnode_fix_remove(struct cl_rbnode *q,
	struct cl_rbnode *p, struct cl_rbnode *g, cl_direction_t last)
{
	struct cl_rbnode *s = p->link[!last];
	if(s != NULL) {
		if(cl_rbnode_is_not_red(s->link[LEFT]) &&
			cl_rbnode_is_not_red(s->link[RIGHT]))
		{
			p->color = CL_BLACK;
			s->color = CL_RED;
			q->color = CL_RED;
		} else {
			cl_direction_t d = g->link[RIGHT] == p;
			if(cl_rbnode_is_red(s->link[last]))
				g->link[d] = cl_rbnode_rotate_double(p, last);
			else if(cl_rbnode_is_red(s->link[!last]))
				g->link[d] = cl_rbnode_rotate_single(p, last);
			q->color = CL_RED;
			g->link[d]->color = CL_RED;
			g->link[d]->link[LEFT]->color = CL_BLACK;
			g->link[d]->link[RIGHT]->color = CL_BLACK;
		}
	}
}

static void *cl_rbtree_remove2(struct cl_rbtree *tree, void *value) {
	struct cl_rbnode froot = {0};		/* fake root */
	struct cl_rbnode *q = &froot;		/* iterator */
	struct cl_rbnode *p = NULL;		/* parent */
	struct cl_rbnode *g = NULL;		/* grandparent */
	struct cl_rbnode *f = NULL;		/* found node */
	void *v = NULL;				/* value found */
	cl_direction_t dir = RIGHT;

	froot.link[RIGHT] = tree->root;

	while(q->link[dir] != NULL) {
		cl_direction_t last = dir;
		cl_compare_t c;

		g = p;			/* update grandparent */
		p = q;			/* update parent */
		q = q->link[dir];	/* update iterator */

		c = tree->compare(q->value, value);
		if(c == CL_EQUAL)
			f = q;
		dir = cl_direction(c == CL_LESS);

		if(cl_rbnode_is_not_red(q) &&
			cl_rbnode_is_not_red(q->link[dir]))
		{
			if(cl_rbnode_is_red(q->link[!dir])) {
				p->link[last] = cl_rbnode_rotate_single(q, dir);
				p = p->link[last];
			} else
				cl_rbnode_fix_remove(q, p, g, last);
		}
	}

	if(f != NULL) {
		v = f->value;
		f->value = q->value;
		cl_pool_release(&tree->pool, q);
		tree->n_items--;
		p->link[cl_direction(p->link[RIGHT] == q)] =
			q->link[cl_direction(q->link[LEFT] == NULL)];
	}
	tree->root = froot.link[RIGHT];
	if(tree->root != NULL)
		tree->root->color = CL_BLACK;

	return v;
}

/** Remove an item from a red-black tree.
 *
 * This method will remove the specified item from the red-black tree.
 */
void *cl_rbtree_remove(struct cl_rbtree *tree, void *value) {
	if(tree->root == NULL)
		return NULL;
	else
		return cl_rbtree_remove2(tree, value);
}

static void cl_rbtree_clear_node(struct cl_rbtree *tree,
	struct cl_rbnode *n, CL_CALLBACK *call, void *bound)
{
	if(n != NULL) {
		cl_rbtree_clear_node(tree, n->link[LEFT], call, bound);
		if(call)
			call(n->value, bound);
		cl_rbtree_clear_node(tree, n->link[RIGHT], call, bound);
		cl_pool_release(&tree->pool, n);
	}
}

/** Clear a red-black tree.
 *
 * This method will remove all items from the red-black tree.
 */
void cl_rbtree_clear(struct cl_rbtree *tree, CL_CALLBACK *call, void *bound) {
	cl_rbtree_clear_node(tree, tree->root, call, bound);
	tree->root = NULL;
	tree->n_items = 0;
	cl_pool_destroy(&tree->pool);
}

static void cl_rbtree_iterate(struct cl_rbtree *tree, struct cl_rbnode *n,
	CL_CALLBACK *call, void *bound)
{
	if(n != NULL) {
		cl_rbtree_iterate(tree, n->link[LEFT], call, bound);
		call(n->value, bound);
		cl_rbtree_iterate(tree, n->link[RIGHT], call, bound);
	}
}

/** Iterate over a red-black tree.
 *
 * This method will iterate over each item in the red-black tree. The closure 
 * will be called for each item.
 */
void cl_rbtree_for_each(struct cl_rbtree *tree, CL_CALLBACK *call, void *bound)
{
	cl_rbtree_iterate(tree, tree->root, call, bound);
}

/** Get (peek) the first item in the tree.
 *
 * This method will get (peek at) the first item stored in the red-black tree.
 */
void *cl_rbtree_peek(struct cl_rbtree *tree) {
	struct cl_rbnode* n = tree->root;
	if(n == NULL)
		return NULL;
	while(n->link[LEFT] != NULL)
		n = n->link[LEFT];
	return n->value;
}

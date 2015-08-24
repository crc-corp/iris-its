#ifndef TIMER_H
#define TIMER_H

#include <signal.h>	/* for struct sigaction */
#include <sys/time.h>	/* for struct itimerval */

enum {
	PIPE_READ = 0,
	PIPE_WRITE = 1
};

struct timer {
	struct itimerval	itimer;
	int			pipe_fd[2];
	struct sigaction	sa_original;
};

struct timer *timer_init();
void timer_destroy();
int timer_arm(unsigned int msec);
int timer_disarm();
int timer_read();
int timer_get_fd();

#endif

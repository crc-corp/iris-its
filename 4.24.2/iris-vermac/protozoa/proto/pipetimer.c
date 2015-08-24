#include <signal.h>
#include <stdio.h>
#include <sys/time.h>

enum {
	PIPE_READ = 0,
	PIPE_WRITE = 1
};

int timer_pipe[2];

static void timer_handler(int signo) {
	static const char c = 0;

	write(timer_pipe[PIPE_WRITE], &c, 1);
}

int main(int argc, char* argv[]) {

	struct sigaction sa;
	struct itimerval itimer;
	char c;

	pipe(timer_pipe);

	sa.sa_handler = &timer_handler;
	sa.sa_flags = SA_RESTART;
	sigfillset(&sa.sa_mask);
	sigaction(SIGALRM, &sa, NULL);

	itimer.it_interval.tv_sec = 0;
	itimer.it_interval.tv_usec = 250000;
	itimer.it_value.tv_sec = 0;
	itimer.it_value.tv_usec = 250000;
	setitimer(ITIMER_REAL, &itimer, NULL);

	while(1) {
		read(timer_pipe[PIPE_READ], &c, 1);
		printf("read 1 byte\n");
	}
}

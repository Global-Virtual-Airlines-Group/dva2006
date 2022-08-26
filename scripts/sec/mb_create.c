#include <errno.h>
#include <pwd.h>
#include <stdio.h>
#include <string.h>
#include <syslog.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

void mkfolder(const char* path, const char* name) {

char fp[320];
struct passwd* pwd = getpwnam("postfix"); 

snprintf(fp, sizeof(fp), "%s/%s", path, name);
mkdir(fp, S_IRWXU | S_IRWXG);
pwd = getpwnam("postfix");
if (pwd) {
	if (chown(fp, pwd->pw_uid, pwd->pw_gid) == -1)
		syslog(LOG_ERR, "Error setting %s owner - %s", fp, strerror(errno));
	else
		syslog(LOG_INFO, "Set permissions on %s", fp);
}
}

int main(int argc, char **argv) {

if (argc < 3) {
	printf("mb_create <mailbox> <path> ...\n");
	return 1;
}

openlog("mbcreate", LOG_PID | LOG_PERROR, LOG_USER); 

struct stat st = {0};
if (stat(argv[2], &st) == -1) {
	printf("%s does not exist\n", argv[2]);
	return 2;
}

char fullpath[256];
snprintf(fullpath, sizeof(fullpath), "%s/%s", argv[2], argv[1]);
if (stat(fullpath, &st) == -1) {
	mkfolder(argv[2], argv[1]);
	mkfolder(fullpath, "cur");
	mkfolder(fullpath, "new");
	mkfolder(fullpath, "tmp");
	syslog(LOG_INFO, "Created %s", fullpath);
} else
	syslog(LOG_NOTICE, "%s already exists!\n", fullpath);

char spampath[256];
snprintf(spampath, sizeof(spampath), "%s/%s", fullpath, ".!SPAM");
if (stat(spampath, &st) == -1) {
	mkfolder(fullpath, ".SPAM");
	mkfolder(spampath, "cur");
	mkfolder(spampath, "new");
	mkfolder(spampath, "tmp");
	syslog(LOG_INFO, "Created %s", spampath);
} else
	syslog(LOG_NOTICE, "%s already exists!\n", spampath);

closelog();
return 0;
}

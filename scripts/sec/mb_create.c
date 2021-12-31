#include <errno.h>
#include <pwd.h>
#include <stdio.h>
#include <string.h>
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
		printf("Error setting %s owner - %s\n", fp, strerror(errno)); 
}
}

int main(int argc, char **argv) {

if (argc < 3) {
	printf("mb_create <mailbox> <path> ...\n");
	return 1;
}

struct stat st = {0};
if (stat(argv[2], &st) == -1) {
	printf("%s does not exist\n", argv[2]);
	return 2;
}

char fullpath[256];
snprintf(fullpath, sizeof(fullpath), "%s/%s", argv[2], argv[1]);
if (stat(fullpath, &st) != -1) {
	printf("%s already exists!\n", fullpath);
	return 3;
}

mkfolder(argv[2], argv[1]);
mkfolder(fullpath, "cur");
mkfolder(fullpath, "new");
mkfolder(fullpath, "tmp");

char spampath[256];
snprintf(spampath, sizeof(spampath), "%s/%s", fullpath, ".SPAM");
mkfolder(fullpath, ".SPAM");
mkfolder(spampath, "cur");
mkfolder(spampath, "new");
mkfolder(spampath, "tmp");

return 0;
}


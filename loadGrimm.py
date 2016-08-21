# The sole purpose of this script is to take the RomeoAndJuliet.txt file and push each line
# into a redis database so that localMessaging.scala can read from it and have realistic looking
# conversations. This script will not have to be run multiple times, one once on one redis
# server which will be accessed remotely by the Actors

import redis

RedisDB = redis.StrictRedis(host='localhost', port=6379, db=0)
shakespeareFile = open("grimm_fairy_tales.txt", "r")

# We add it to the database 10x because we will have a huge number of conversations which will
# quickly eat up all the lines in grimm_fairy_tales.txt
for x in range(10):
	for line in shakespeareFile.readlines():
		RedisDB.rpush("server:grimm_fairy_tales", line)
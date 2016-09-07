import socket
import redis

def getIP():
	socketServer = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	socketServer.connect(("gmail.com", 80))
	return socketServer.getsockname()[0]
	socketServer.close()

def addIPToRedis(ip):
	RedisDB = redis.StrictRedis(host="52.35.37.109", port=6379, db=0)
	RedisDB.rpush("server:ActorMasterListIPs", ip)

ip = getIP()
addIPToRedis(ip)

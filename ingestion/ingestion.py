#!/usr/bin/python

from __future__ import print_function
import twitter
import time
import ConfigParser
import sys
from datetime import date, datetime, timedelta
import mysql.connector



def api():
    api = twitter.Api(consumer_key="qWVMWBNdGu4PibPH13CtzA", consumer_secret="X23D2dWqNY3t7XoMYF3sLcL2yT9LLBQgazsyT964", access_token_key="17902263-JNXCNI6s0zqdqxJL6Tbz2Rlh32tPYbed2KJwZALOr", access_token_secret="hrXRb1cObhst4LIZJyDy6JyVsvyvUpzTqi1hnZVHHW2sq")
    #print api.VerifyCredentials()
    return api

def readConfig():
    conf = ConfigParser.ConfigParser()
    conf.read("/Users/agore/.linkharvester/ids.conf")
    if conf.has_section("ids"):
        max_id = conf.getint("ids", "max_id")
        since_id = conf.getint("ids", "since_id")
        return (max_id, since_id)
    else:
        return (0, 0)

def writeConfig(max_id = 0, since_id = 0):
    cf = open("/Users/agore/.linkharvester/ids.conf", "w")
    conf = ConfigParser.ConfigParser()
    conf.add_section("ids")
    conf.set("ids", "max_id", max_id)
    conf.set("ids", "since_id", since_id)
    conf.write(cf)
    cf.close()

def getStatuses(api, max_id=0, since_id=0, count=20):
    if max_id != 0 and since_id == 0: 
        statuses = api.GetHomeTimeline(count=count, max_id = max_id)
    elif max_id == 0 and since_id != 0: 
        statuses = api.GetHomeTimeline(count=count, since_id = since_id)
    else :
        statuses = api.GetHomeTimeline(count = count)
    return statuses

def processStatus(status):
    urls = status.urls
    url_string = ""
    for url in urls:
        url_string += str(url.expanded_url) + "\t"

    ts = time.strftime("%Y-%m-%d %H:%M:%S", time.strptime(status.created_at, "%a %b %d %H:%M:%S +0000 %Y"))
    tweet = "%s\t%s\t%s\t%s\t%s\t%s\t%s\n" % (status.id, status.user.name, status.user.screen_name, status.text, status.user.profile_image_url, ts, url_string)
    print(tweet.encode('utf-8'))

def dumpStatusesToDb(statuses):
    cnx = mysql.connector.connect(user="aditya", database="lh", charset="utf8mb4")
    cursor = cnx.cursor()
    for status in statuses:
        urls = status.urls
        if len(urls) > 0:
            #url1 = str(urls[0].expanded_url)
            url1 = urls[0].expanded_url
        else :
            url1 = ""
        if len(urls) > 1 :
            #url2 = str(urls[1])
            url2 = urls[1].expanded_url
        else :
            url2 = ""
        ts = time.strftime("%Y-%m-%d %H:%M:%S", time.strptime(status.created_at, "%a %b %d %H:%M:%S +0000 %Y"))
        tweet_insert = ("INSERT INTO tweet (id, name, screen_name, tweet, avatar_url, ts, url1, url2) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)")
        tweet_data = (status.id, status.user.name, status.user.screen_name, status.text, status.user.profile_image_url, ts, url1, url2)
        cursor.execute(tweet_insert, tweet_data)
    cnx.commit()
    cursor.close()
    cnx.close()

if __name__ == '__main__':    
    MAX = 5
    api = api()
    (max_id, since_id) = readConfig()
    statuses = getStatuses(api, count = 200, since_id=since_id)
    total = 0
    if statuses is None or len(statuses) == 0:
        print("No statuses", file=sys.stderr)
    else :
        try :
            dumpStatusesToDb(statuses)
            since_id = statuses[0].id
            temp_max_id = statuses[len(statuses) - 1].id - 1
            loop_count = 1
            total = len(statuses)
            while (True):
                if (max_id == 0 or temp_max_id < max_id or loop_count == MAX) :
                    max_id = since_id
                    break
                statuses = getStatuses(api, count=200, max_id=temp_max_id)
                if statuses is None or len(statuses) == 0: 
                    break
                total = total + len(statuses)
                temp_max_id = statuses[len(statuses) - 1].id - 1
                loop_count += 1
        finally:
            if total is None:
                total = 0
            print ("Wrote %d records\n" % total)
            writeConfig(max_id, since_id)    

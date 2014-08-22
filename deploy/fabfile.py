from fabric.contrib.project import rsync_project
from fabric.api import *

dist = "snake-1.0-SNAPSHOT"

env.hosts = ['play-snake.com']

def deploy():
    local("unzip ../target/universal/"+dist+".zip") 
    put("upstart/snake.conf", "/etc/init/", use_sudo=True)
    with settings(warn_only=True):
        run("sudo stop snake")
    rsync_project(local_dir=dist+"/", remote_dir="/root/snake", delete=True)
    run("sudo start snake")
    local("rm -r "+dist)

def package():
    local("rm -rf "+dist+".zip")
    local("rm -rf "+dist)
    local("rm -rf package")
    with lcd("../"):
        local("./activator dist")
    local("unzip ../target/universal/"+dist+".zip") 
    local("mv "+dist+" package")








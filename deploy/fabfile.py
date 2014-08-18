from fabric.contrib.project import rsync_project
from fabric.api import *

dist = "snake-1.0-SNAPSHOT"

def deploy():
    local("unzip ../target/universal/"+dist+".zip") 
    rsync_project(local_dir=dist+"/", remote_dir="/root/snake", delete=True)
    put("upstart/snake.conf", "/etc/init/", use_sudo=True)
    with settings(warn_only=True):
        run("sudo stop snake")
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








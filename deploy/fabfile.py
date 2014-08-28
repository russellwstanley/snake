from fabric.api import *

dist = "snake-1.0-SNAPSHOT"

def package():
    local("rm -rf "+dist+".zip")
    local("rm -rf "+dist)
    local("rm -rf package")
    with lcd("../"):
        local("./activator dist")
    local("unzip ../target/universal/"+dist+".zip") 
    local("mv "+dist+" package")








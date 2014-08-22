$(document).ready(function() {
    var snakeWidth = 10;
    var canvas = $("#canvas")[0];
    var context = canvas.getContext("2d");
    wsEndpoint = location.host+':9000';
    //TODO this is ugly
    if(location.host.indexOf(':') > -1){
        wsEndpoint = location.host; //do not append the port if already connecting on a non default port
    }
    soc = new WebSocket("ws://"+wsEndpoint+"/socket");
    soc.onmessage = function(event){
        points = JSON.parse(event.data);
        console.log(event.data)
        //context.clearRect(0, 0, canvas.width, canvas.height);
        addedPoints = points[0]
        deletedPoints = points[1]
        //TODO remove duplication
        for(j=0;j<addedPoints.length;j++){
            xpos = addedPoints[j][0] * snakeWidth;
            ypos = addedPoints[j][1] * snakeWidth;
            context.fillRect(xpos,ypos ,snakeWidth,snakeWidth);
        }
        for(i=0;i<deletedPoints.length;i++){
            xpos = deletedPoints[i][0] * snakeWidth;
            ypos = deletedPoints[i][1] * snakeWidth;
            context.clearRect(xpos,ypos ,snakeWidth,snakeWidth);
        }
    };

    $(document).on("keypress",function(event){
        if(event.keyCode == 106){
            soc.send("l");
        }
        else if(event.keyCode == 108){
            soc.send("r");
        }
        else{
            console.log("unsupported keypress");
        }
    });
});


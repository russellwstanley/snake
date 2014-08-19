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
        context.clearRect(0, 0, canvas.width, canvas.height);
        for(j=0;j<points.length;j++){
            xpos = points[j][0] * snakeWidth;
            ypos = points[j][1] * snakeWidth;
            context.fillRect(xpos,ypos ,snakeWidth,snakeWidth);
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


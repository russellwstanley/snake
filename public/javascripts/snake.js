$(document).ready(function() {
    var snakeWidth = 10;
    var canvas = $("#canvas")[0];
    var context = canvas.getContext("2d");
    soc = new WebSocket("ws://"+location.host+"/socket");
    soc.onmessage = function(event){
        snakes = JSON.parse(event.data);
        context.clearRect(0, 0, canvas.width, canvas.height);
        for(i=0;i<snakes.length; i++){
            points = snakes[i];
            for(j=0;j<points.length;j++){
                xpos = points[j].x * snakeWidth;
                ypos = points[j].y * snakeWidth;
                context.fillRect(xpos,ypos ,snakeWidth,snakeWidth);
            }
        }
        console.log(event.data);
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


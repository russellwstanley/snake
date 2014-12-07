$(document).ready(function() {
    var snakeWidth = 10;
    $("canvas").each(function(i){
    var id = $(this).attr('id');
    var canvas = $(this)[0];
    var context = canvas.getContext("2d");
    wsEndpoint = location.host+':9000';
    //TODO this is ugly
    if(location.host.indexOf(':') > -1){
        wsEndpoint = location.host; //do not append the port if already connecting on a non default port
    }
    soc = new WebSocket("ws://"+wsEndpoint+"/gamesocket/"+id);
    soc.onmessage = function(event){
        points = JSON.parse(event.data);
        clearPoints(points[1],context);
        addPoints(points[0],context);
    };

    addPoints = function(points,context){
        for(i=0;i<points.length;i++){
            xpos = points[i].x * snakeWidth;
            ypos = points[i].y * snakeWidth;
            color = points[i].c;
            console.log(color);
            context.fillStyle = color;
            context.fillRect(xpos,ypos ,snakeWidth,snakeWidth);
        }
    };
    clearPoints = function(points,context){
        for(i=0;i<points.length;i++){
            xpos = points[i].x * snakeWidth;
            ypos = points[i].y * snakeWidth;
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
    });

    $(document).on("scrollstart",false);

    $(document).on("swipeleft",function(event){
        event.stopPropagation();
        event.preventDefault();
        soc.send("l");
    });
    $(document).on("swiperight",function(event){
        event.stopPropagation();
        event.preventDefault();
        soc.send("r");
    });
    })

});


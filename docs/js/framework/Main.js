/**
* ElkPublic is a JavaScript library that makes it easier
* to manage the HTML of your application and
*  interact with the Java servlet ElkServer.
* More details at <https://github.com/tncrazvan/ElkServer/>.
* Copyright (C) 2016-2018  Tanase Razvan Catalin
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

function getJobLocation(){
    var currentLocation = "", i = 0;
    var exp = window.location.pathname.split("/");
    var temp = Project.workspace.split("/");
    foreach(exp,function(item){
        if(i===temp.length){
            currentLocation +=item;
        }else if(i>temp.length){
            currentLocation += "/"+item;
        }
        i++;
    });
    var tmp = location.href.split(currentLocation+"?");
    if(tmp.length > 1){
      if(tmp[1] !== ""){
        currentLocation += "?"+tmp[1];
      }
    }
    return currentLocation;
}


function R(){}
R.cls=function(cls){
  if(isset(cls) && cls !== "")
    return document.getElementsByClassName(cls);
  else
    return null;
};
R.id=function(id){
  if(isset(id) && id !== "")
    return document.getElementById(id);
  else
    return null;
};
R.new=function(object){
  return document.createElement(object);
};


function getProtocol(){
  return window.location.href.split("://")[0];
}

function isset(object){
    if(object!==undefined){
        return true;
    }else{
        return false;
    }
}

function isnull(object){
    if(object === null) {
        return true;
    } else {
        return false;
    }
}

function isempty(object){
    if(object===""){
        return true;
    }else{
        return false;
    }
}

function isFunction(functionToCheck) {
 var getType = {};
 return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
}

function isElement(obj) {
  try {
    //Using W3 DOM2 (works for FF, Opera and Chrome)
    return obj instanceof HTMLElement;
  }
  catch(e){
    //Browsers not supporting W3 DOM2 don't have HTMLElement and
    //an exception is thrown and we end up here. Testing some
    //properties that all elements have (works on IE7)
    return (typeof obj==="object") &&
      (obj.nodeType===1) && (typeof obj.style === "object") &&
      (typeof obj.ownerDocument ==="object");
  }
}

function create(tag,content,allowVariables){
    var element = document.createElement(tag);
    if(isset(content)){
      if(isElement(content)){
        element.innerHTML = "";
        element.appendChild(content);
      }else{
        element.applyHtml(content,allowVariables);
      }
    }
    return element;
}

function Box(tag, $function) {
    this.e = document.createElement(tag);
    this.add = function (other) {
        if (other instanceof Box) {
            this.e.appendChild(other.e);
        } else {
            this.e.appendChild(other);
        }
    };
    this.find=function(){
        return this.e;
    };
    if(isset($function)){
        ($function)(this.e);
    }
}




function PostHttpEvent(uri,success,data,other){
  return new HttpEvent(uri,success,other,"POST",data);
}

//INFORMATINOAL RESPONSES
HttpEvent.STATUS_CONTINUE = "100 Continue";
HttpEvent.STATUS_SWITCHING_PROTOCOLS = "101 Switching Protocols";
HttpEvent.STATUS_PROCESSING = "102 Processing";

//SUCCESS
HttpEvent.STATUS_SUCCESS = "200 OK";
HttpEvent.STATUS_CREATED = "201 CREATED";
HttpEvent.STATUS_ACCEPTED = "202 ACCEPTED";
HttpEvent.STATUS_NON_AUTHORITATIVE_INFORMATION = "203 Non-Authoritative Information";
HttpEvent.STATUS_NO_CONTENT = "204 No Content";
HttpEvent.STATUS_RESET_CONTENT = "205 Reset Content";
HttpEvent.STATUS_PARTIAL_CONTENT = "206 Partial Content";
HttpEvent.STATUS_MULTI_STATUS = "207 Multi-Status";
HttpEvent.STATUS_ALREADY_REPORTED = "208 Already Reported";
HttpEvent.STATUS_IM_USED = "226 IM Used";

//REDIRECTIONS
HttpEvent.STATUS_MULTIPLE_CHOICES = "300 Multiple Choices";
HttpEvent.STATUS_MOVED_PERMANENTLY = "301 Moved Permanently";
HttpEvent.STATUS_FOUND = "302 Found";
HttpEvent.STATUS_SEE_OTHER = "303 See Other";
HttpEvent.STATUS_NOT_MODIFIED = "304 Not Modified";
HttpEvent.STATUS_USE_PROXY = "305 Use Proxy";
HttpEvent.STATUS_SWITCH_PROXY = "306 Switch Proxy";
HttpEvent.STATUS_TEMPORARY_REDIRECT = "307 Temporary Redirect";
HttpEvent.STATUS_PERMANENT_REDIRECT = "308 Permanent Redirect";

//CLIENT ERRORS
HttpEvent.STATUS_BAD_REQUEST = "400 Bad Request";
HttpEvent.STATUS_UNAUTHORIZED = "401 Unauthorized";
HttpEvent.STATUS_PAYMENT_REQUIRED = "402 Payment Required";
HttpEvent.STATUS_FORBIDDEN = "403 Forbidden";
HttpEvent.STATUS_NOT_FOUND = "404 Not Found";
HttpEvent.STATUS_METHOD_NOT_ALLOWED = "405 Method Not Allowed";
HttpEvent.STATUS_NOT_ACCEPTABLE = "406 Not Acceptable";
HttpEvent.STATUS_PROXY_AUTHENTICATION_REQUIRED = "407 Proxy Authentication Required";
HttpEvent.STATUS_REQUEST_TIMEOUT = "408 Request Timeout";
HttpEvent.STATUS_CONFLICT = "409 Conflict";
HttpEvent.STATUS_GONE = "410 Gone";
HttpEvent.STATUS_LENGTH_REQUIRED = "411 Length Required";
HttpEvent.STATUS_PRECONDITION_FAILED = "412 Precondition Failed";
HttpEvent.STATUS_PAYLOAD_TOO_LARGE = "413 Payload Too Large";
HttpEvent.STATUS_URI_TOO_LONG = "414 URI Too Long";
HttpEvent.STATUS_UNSUPPORTED_MEDIA_TYPE = "415 Unsupported Media Type";
HttpEvent.STATUS_RANGE_NOT_SATISFIABLE = "416 Range Not Satisfiable";
HttpEvent.STATUS_EXPECTATION_FAILED = "417 Expectation Failed";
HttpEvent.STATUS_IM_A_TEAPOT = "418 I'm a teapot";
HttpEvent.STATUS_MISDIRECTED_REQUEST = "421 Misdirected Request";
HttpEvent.STATUS_UNPROCESSABLE_ENTITY = "422 Unprocessable Entity";
HttpEvent.STATUS_LOCKED = "423 Locked";
HttpEvent.STATUS_FAILED_DEPENDENCY = "426 Failed Dependency";
HttpEvent.STATUS_UPGRADE_REQUIRED = "428 Upgrade Required";
HttpEvent.STATUS_PRECONDITION_REQUIRED = "429 Precondition Required";
HttpEvent.STATUS_TOO_MANY_REQUESTS = "429 Too Many Requests";
HttpEvent.STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE = "431 Request Header Fields Too Large";
HttpEvent.STATUS_UNAVAILABLE_FOR_LEGAL_REASONS = "451 Unavailable For Legal Reasons";

//SERVER ERRORS
HttpEvent.STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error";
HttpEvent.STATUS_NOT_IMPLEMENTED = "501 Not Implemented";
HttpEvent.STATUS_BAD_GATEWAY = "502 Bad Gateway";
HttpEvent.STATUS_SERVICE_UNAVAILABLE = "503 Service Unavailable";
HttpEvent.STATUS_GATEWAY_TIMEOUT = "504 Gateway Timeout";
HttpEvent.STATUS_HTTP_VERSION_NOT_SUPPORTED = "505 HTTP Version Not Supported";
HttpEvent.STATUS_VARIANT_ALSO_NEGOTATIES = "506 Variant Also Negotiates";
HttpEvent.STATUS_INSUFFICIENT_STORAGE = "507 Insufficient Storage";
HttpEvent.STATUS_LOOP_DETECTED = "508 Loop Detected";
HttpEvent.STATUS_NOT_EXTENDED = "510 Not Extended";
HttpEvent.STATUS_NETWORK_AUTHENTICATION_REQUIRED = "511 Network Authentication Required";

function GetHttpEvent(uri,success,other){
  return new HttpEvent(uri,success,other,"GET",{});
}

function HttpEvent(uri,success, other, type, data) {
    $this = this;
    this.status;
    this.other = other; //other stuff such as listeners, ecc (check ajax progress listener below for that regard)
    success = (isset(success) ? success : function () {});

    var url = Project.workspace+uri;
    var type = isset(type)?type:'GET'; //default transmission method
    var dataType = 'text/plain';
    var contentType = 'application/json';


    this.setUrl = function (value) {
        url = value;
    };


    this.setDataType = function (value) {
        dataType = value;
    };

    this.setContentType = function (value) {
        contentType = value;
    };

    this.getUrl = function () {
        return url;
    };

    this.getType = function () {
        return type;
    }, this.getMethod = this.getType;

    this.getDataType = function () {
        return dataType;
    };

    this.getContentType = function () {
        return contentType;
    };

    var requestHeaders = {};
    this.setRequestHeader=function(headers){
      requestHeaders = headers;
    };
    this.run = function () {

        var formdata = new FormData();  //new storage for properly formatted json array/object to flush
        for (var key in data) {
            try{
              formdata.append(key, data[key].btoa());
            }catch(e){
              console.log("Failed btoa() attempt on: "+data[key]);
            }
        }

        var xhr = new XMLHttpRequest();
        //set events here


        if ($this.other) {
            for (var key in $this.other) {

            	switch(key){
            	/**************DOWNLOAD******************/
                    case "downloadProgress":
                        xhr.addEventListener('progress', function (event) {
                            (other[key])(event);
                        }, false);
                    break;
                    case "downloadComplete":
                        xhr.addEventListener('load', function (event) {
                            (other[key])(event);
                        }, false);
                        break;
                    case "downloadError":
                        xhr.addEventListener('error', function (event) {
                            (other[key])(event);
                        }, false);
                        break;
                    case "downloadAbort":
                        xhr.addEventListener('abort', function (event) {
                            (other[key])(event);
                        }, false);
                        break;
            	/*****************UPLOAD***********************/
                    case "uploadProgress":
                        xhr.upload.addEventListener('progress', function (event) {
                            (other[key])(event);
                        }, false);
                        break;
                    case "uploadComplete":
                        xhr.upload.addEventListener('load', function (event) {
                            (other[key])(event);
                        }, false);
                        break;
                    case "uploadError":
                        xhr.upload.addEventListener('error', function (event) {
                            (other[key])(event);
                        }, false);
                        break;
                    case "uploadAbort":
                        xhr.upload.addEventListener('abort', function (event) {
                            (other[key])(event);
                        }, false);
                        break;

            	}

            }
        }
        xhr.onreadystatechange = function () { //whenever state changes
            if (this.readyState === 4){ //onready state run
                $this.status = this.status+" "+this.statusText;
                (success)(xhr.responseText,$this.status);
            }
        };


        xhr.open(this.getMethod(), this.getUrl(),true);
        for (var key in requestHeaders) {
           if (requestHeaders.hasOwnProperty(key)) {
              xhr.setRequestHeader(key,requestHeaders[key]);
           }
        }
        xhr.send(formdata); //run
    };
}

function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

//this function will try and find one leaf within
//the window variables tree (given the "string" path) and return it
function TMP55341(strings){
  var tmp,counter=0;
  for(var key in strings){
    if (strings.hasOwnProperty(key)){
      if(counter===0){
        tmp = window[strings[key]];
      }else{
        tmp = tmp[strings[key]];
      }
    }
    counter++;
  }
  return tmp;
}

function GETENTITYHRTY3634FDV(string){
  return TMP55341(string.substring(0).split(/\./g));
}



//this function will parse for inline html variables inside the given #text nodes
function PARSEVAR55TH72(child){
  //assign content to tmp variable
  var entity = GETENTITYHRTY3634FDV(child.getAttribute("$"));

  //if it's an object...
  if(typeof entity === "object"){
    //"if it has a tag name..." (aka: "if it's a dom element...")
    if(isset(entity.nodeName)){
      child.parentNode.insertBefore(entity,child);
      child.remove();
    }else{
      //console.log("This is not a node, but it is an object.");
      child.innerHTML = JSON.stringify(entity);
      //console.log(PARSEVAR55TH72.tmpArray[key].substring(1));
    }
  }
}

//This function will parse the dom item and replace
//its contents with the relative solution in the vocabulary
function PARSEVOCABULARY3100JJYT6(item){
  tmp = item.getAttribute("@").split("/");
  if(tmp.length === 1){
    tmp[1] = tmp[0];
    tmp[0] = "*";
  }
  if(tmp.length > 0){
    try{
      item.innerHTML = vocabulary.page[tmp[0]].phrase[tmp[1]].lang[localStorage.getItem("language")];
    }catch(error){
      console.error(error)
      console.error(tmp);
    }
  }
}

//This function will parse the contents of the given children of the dom and
//replace its contents with the relative solution in the vocabulary
function PARSEVOCABULARYCHILDREN347HHH7J(children, allowVariables){
  let scripts = "";
  foreach(children,function(child){
    if(child.nodeName[0] !== "#"){
      if(child.nodeName === "SCRIPT"){
          //save <script> contents to variable for later use
          scripts += child.innerHTML;
      }else if(child.hasAttribute("@")){
        tmp = child.getAttribute("@").split("/");
        if(tmp.length === 1){
          tmp[1] = tmp[0];
          tmp[0] = "*";
        }
        if(tmp.length > 0){
          try{
            child.innerHTML = vocabulary.page[tmp[0]].phrase[tmp[1]].lang[localStorage.getItem("language")];
          }catch(error){
            console.error(error)
            console.error(tmp);
          }

        }
      }else if(child.hasAttribute("$")){
        PARSEVAR55TH72(child);
      }else{
        PARSEVOCABULARYCHILDREN347HHH7J(child.childNodes,allowVariables);
      }
    }
  });
  if(scripts.trim() !== "") eval(scripts);
}

//iterating through every child node of the provided target
function RECURSIVE76349AAD(target,allowVariables){
  let scripts = "";
  foreach(target.childNodes,function(item){ //iterating through each tag
      //find <script>
      if(item.nodeName === "SCRIPT"){
          //save <script> contents to variable for later use
          scripts += item.innerHTML;
      }else if(item.nodeName[0] !== "#"){ //if it's not some type of string or comment...
        //if this current element has an "id" attribute set to something...
        if(item.hasAttribute("id")){
            //...save element on the window object
            //I'm not directly saving "item" into window because the node
            //doesn't exist inside the dom at this point,
            //I need to find the element after it's been created.
            //"R.id()" will do this for me.
            window[item.getAttribute("id")] = item;
        }
        //check if this item has an attribute named "@"
        if(item.hasAttribute("@")){
          PARSEVOCABULARY3100JJYT6(item);
        }else if(item.hasAttribute("$")){
          PARSEVAR55TH72(item);
        }else{ //if it doesn't...
          //keep going and try to parse its children
          PARSEVOCABULARYCHILDREN347HHH7J(item.childNodes,allowVariables);

        }
      }
  });
  if(scripts.trim() !== "") eval(scripts);
}

var HttpPromise = function(uri){
  return new GetHttpPromise(uri);
};

var GetHttpPromise = function(uri){
  return new GetPromise(uri);
};

var PostHttpPromise = function(uri,data){
  return new PostPromise(uri,data);
};

var GetPromise = function(uri){
  return new Promise(function(resolve,reject){
    new HttpEvent(uri,function(result,status){
      this.status = status;
      (resolve)(result);
    }).run();
  });
};

var PostPromise = function(uri,data){
  return new Promise(function(resolve,reject){
    new PostHttpEvent(uri,function(result,status){
      this.status = status;
      (resolve)(result);
    },data).run();
  });
};

var Job = HttpEvent;

function applyHtml(target,data,allowVariables){
    //pushing data to the target
    //NOTE: just pushing html text into an element won't execute
    //the scripting inside the data, it will just print it as plain
    //text and ignore it. The parsing is being done below inside the foreach cycle.

    //target.innerHTML = data;

    //temporary parent element
    //I'm using this to throw in the result data
    //and parse it as child nodes.

    allowVariables = (isset(allowVariables)?allowVariables:false);
    target.innerHTML = data;
    RECURSIVE76349AAD(target,allowVariables);
}

function foreachChild(children,f){
  foreach(children,function(child){
    if(child.childNodes.length > 0){
      foreachChild(child.childNodes,f);
    }else if(child.nodeName !== "#text" && child.nodeName !== "#comment"){
      (f)(child);
    }
  });
}

fx.i = 0;
fx.delay = 0;
fx.before = function () {};
fx.after = function () {};
fx.target = document.getElementById("main");
function fx(focus, onready, target) {
    if(!isset(onready)) onready = function(){};
    var $this=this;
    if (!isset(target)) {
        this.target = fx.target;
    }else{
	     this.target = target;
    }
    (fx.before)(this.target);

    var j = new HttpEvent("/@"+focus,function(r){
    	setTimeout(function(){
            applyHtml($this.target,r);
            (fx.after)($this.target);
        },fx.delay);

    });

    j.run();
}

function go(link, onready, target) {
	//modifying client url,
    //this is absolutelly not required, it just refreshes the client's
    //url in order to let them know their location or be able to copy it
    history.pushState(null, document.title, Project.workspace + '/' + link);
	//writes new content (with some flashy animation)
    fx(link, onready, target);
	//saves new location
    window.JobLocation=link;
}

//basically does the same thing as go(ling, onready, target), but it's more straight forward
function setContent(uri,target,changeState,allowVariables){
  return new Promise(function(resolve,reject){
    new HttpEvent("/@"+uri,function(result,status){
      if(status === HttpEvent.STATUS_SUCCESS){
        if(isset(changeState)){
          if(changeState){
            history.pushState(null, document.title, Project.workspace + '/' + uri);
            window.JobLocation=uri;
          }
        }
        target.applyHtml(result,allowVariables);
      }else{
        target.applyHtml("Server status message:"+status);
      }
      (resolve)();
    }).run();
  });

}

//basically does the same thing as go(ling, onready, target), but it's more straight forward
function file_get_contents(uri){
  return new Promise(function(resolve,reject){
    new HttpEvent(uri,function(result,status){
      this.status = status;
      (resolve)(result);
    }).run();
  });

}

function forevery(array, $function, counter, from, to) {
    var i;
    var isLast = false;
    for (i = (from ? from : 0); i < (to ? to : array.length); i += counter) {
        isLast=!(i+1 < (to ? to : array.length));
        $function(array[i],i,isLast);
    }
}

function foreach(array, $function, from, to) {
    forevery(array, $function, 1, from, to);
}

function foreachr(array, $function) {
    var i;
    for (i = array.length - 1; i >= 0; i--) {
        var element = array[i];
        $function(element);
    }
}

function notify(message) {
    // Let's check if the browser supports notifications
    if (!("Notification" in window)) {
        alert("This browser does not support desktop notification");
    }

    // Let's check whether notification permissions have already been granted
    else if (Notification.permission === "granted") {
        // If it's okay let's create a notification
        var notification = new Notification(message);
    }

    // Otherwise, we need to ask the user for permission
    else if (Notification.permission !== 'denied') {
        Notification.requestPermission(function (permission) {
            // If the user accepts, let's create a notification
            if (permission === "granted") {
                var notification = new Notification(message);
            }
        });
    }
    // At last, if the user has denied notifications, and you
    // want to be respectful there is no need to bother them any more.
}

window.onpopstate = function () {
    JobLocation = getJobLocation();
    fx(JobLocation);
};
Mouse.leftButtonDown = false;
Mouse.rightButtonDown = false;
Mouse.leftButtonDownX = null;
Mouse.leftButtonDownZ = null;
document.body.onmousedown = function (event) {
    e = event || window.event;
    switch (e.which) {
        case 1:
            Mouse.leftButtonDown = true;
            Mouse.leftButtonDownX = Mouse.position.x;
            Mouse.leftButtonDownY = Mouse.position.y;
            break;
        case 2:

            break;
        case 3:
            Mouse.rightButtonDown = true;
            break;
        default:

            break;
    }
};

document.body.onmouseup = function (event) {
    e = event || window.event;
    switch (e.which) {
        case 1:
            Mouse.leftButtonDown = false;
            Mouse.leftButtonDownX = null;
            Mouse.leftButtonDownY = null;
            break;
        case 2:

            break;
        case 3:
            Mouse.rightButtonDown = false;
            break;
        default:

            break;
    }
};

Mouse.position = {x: null, y: null};
(function () {   //updating mouse position every 10ms
    document.onmousemove = handleMouseMove;
    (function poll() {
        setTimeout(function () {
            getMousePosition();
            poll();
        }, 10);
    })();

    function handleMouseMove(event) {
        var dot, eventDoc, doc, body, pageX, pageY;

        event = event || window.event; // IE-ism

        // If pageX/Y aren't available and clientX/Y are,
        // calculate pageX/Y - logic taken from jQuery.
        // (This is to support old IE)
        if (event.pageX == null && event.clientX != null) {
            eventDoc = (event.target && event.target.ownerDocument) || document;
            doc = eventDoc.documentElement;
            body = eventDoc.body;

            event.pageX = event.clientX +
                    (doc && doc.scrollLeft || body && body.scrollLeft || 0) -
                    (doc && doc.clientLeft || body && body.clientLeft || 0);
            event.pageY = event.clientY +
                    (doc && doc.scrollTop || body && body.scrollTop || 0) -
                    (doc && doc.clientTop || body && body.clientTop || 0);
        }

        Mouse.position = {
            x: event.pageX,
            y: event.pageY
        };
    }
    function getMousePosition() {
        pos = Mouse.position;
        if (!pos) {
            // We haven't seen any movement yet
        } else {
            // Use pos.x and pos.y
            return pos;
        }
    }
})();
function Mouse() {}

Keyboard={
    q: false, w: false, e: false, r: false, t: false, y: false, u: false, i: false, o: false, p: false,
    a: false, s: false, d: false, f: false, g: false, h: false, j: false, k: false, l: false,
    z: false, x: false, c: false, v: false, b: false, n: false, m: false,
    ctrl: false, alt: false, shift: false, space: false, tab: false,
    1: false, 2: false, 3: false, 4: false, 5: false, 6: false, 7: false, 8: false, 9: false, 0: false,
    questionMark: false, equal: false, exclamation: false, quotes: false, quote: false, pound: false, dolar:false, euro: false,
    percent: false, ampersand: false, fSlash: false, bSlash: false, leftParenthesis: false, rightParenthesis: false,
    leftBracket: false, rightBracket: false, circumflex: false, leftBrace: false, rightBrace: false,
    at: false, dot: false, comma: false, semicolon: false, hyphen: false, underscore: false, colon: false,
    esc: false, minus: false, plus: false, star:false, enter: false, del: false, ins: false, end: false,
    home: false, pageUp: false, pageDown: false, backspace: false, arrowLeft: false, arrowRight: false,
    arrowUp: false, arrowDown: false, super: false, meta: false,
    f1: false, f2: false, f3: false, f4: false, f5: false, f6: false, f7: false, f8: false,
    f9: false, f10: false, f11: false, f12: false
};

function Keyboard() {};

function wait(bool,f){
    (function poll(){
        setTimeout(function(){
            if(bool){
                (f)();
            }else{
                poll();
            }
        },1);
    })();
}

Array.prototype.remove = function(deleteValue) {
  for (var i = 0; i < this.length; i++) {
    if (this[i] == deleteValue) {
      this.splice(i, 1);
      i--;
    }
  }
  return this;
};


var getElementOffset = function(element) {
    var top = 0, left = 0;
    do {
        top += element.offsetTop  || 0;
        left += element.offsetLeft || 0;
        element = element.offsetParent;
    } while(element);

    return {
        x: left,
        y: top
    };
};


function Thread(f,t){
  this.run=function(){
    setTimeout(f,t);
  };
}


function getInputCursorPos(input) {
  if ("selectionStart" in input && document.activeElement == input) {
    return {
      start: input.selectionStart,
      end: input.selectionEnd
    };
  }
  else if (input.createTextRange) {
    var sel = document.selection.createRange();
    if (sel.parentElement() === input) {
      var rng = input.createTextRange();
      rng.moveToBookmark(sel.getBookmark());
      for (var len = 0;
      rng.compareEndPoints("EndToStart", rng) > 0;
      rng.moveEnd("character", -1)) {
        len++;
      }
      rng.setEndPoint("StartToStart", input.createTextRange());
      for (var pos = { start: 0, end: len };
      rng.compareEndPoints("EndToStart", rng) > 0;
      rng.moveEnd("character", -1)) {
        pos.start++;
        pos.end++;
      }
      return pos;
    }
  }
  return -1;
}


function getEditableCursorPos(editableDiv) {
  var caretPos = 0,
    sel, range;
  if (window.getSelection) {
    sel = window.getSelection();
    if (sel.rangeCount) {
      range = sel.getRangeAt(0);
      if (range.commonAncestorContainer.parentNode == editableDiv) {
        caretPos = range.endOffset;
      }
    }
  } else if (document.selection && document.selection.createRange) {
    range = document.selection.createRange();
    if (range.parentElement() == editableDiv) {
      var tempEl = document.createElement("span");
      editableDiv.insertBefore(tempEl, editableDiv.firstChild);
      var tempRange = range.duplicate();
      tempRange.moveToElementText(tempEl);
      tempRange.setEndPoint("EndToEnd", range);
      caretPos = tempRange.text.length;
    }
  }
  return caretPos;
}


String.prototype.splice = function(start, delCount, newSubStr) {
    return this.slice(0, start) + newSubStr + this.slice(start + Math.abs(delCount));
};


function selectText(container) {
    if (document.selection) {
        var range = document.body.createTextRange();
        range.moveToElementText(container);
        range.select();
    } else if (window.getSelection) {
        var range = document.createRange();
        range.selectNode(container);
        window.getSelection().addRange(range);
    }
}

Element.prototype.clear=function(){
  this.innerHTML = "";
};

Element.prototype.remove=function(){
    this.oldParent = this.parentElement;
    this.parentElement.removeChild(this);
};
Element.prototype.restore=function(){
    this.oldParent.appendChild(this);
};
function hide(element){
  if(element.style.display.trim() !== "none"){
    if(element.style.display.trim() !== ""){
        element.originalDisplay=element.style.display;
    }else{
        element.originalDisplay="block";
    }
  }
  element.style.display="none";
}

function show(element){
  if(!isset(element.originalDisplay)) element.originalDisplay = "block";
  element.style.display=element.originalDisplay;
}

Element.prototype.showElement=function(){
  if(isset(this.style.display) && this.style.display !== ""){
      this.oldDisplay=this.style.display;
  }else{
      this.oldDisplay="block";
  }

  this.style.display="none";
};

Element.prototype.hideElement=function(){
  if(!isset(this.oldDisplay)) this.oldDisplay = "block";
  this.style.display=this.oldDisplay;
};

Element.prototype.toggleDisplay=function(){
    if(this.style.display!=="none"){
        if(isset(this.style.display) && this.style.display !== ""){
            this.oldDisplay=this.style.display;
        }else{
            this.oldDisplay="block";
        }

        this.style.display="none";
    }else{
        this.style.display=this.oldDisplay;
    }
};

Element.prototype.applyHtml=function(data,allowVariables){
  applyHtml(this,data,allowVariables);
};

Element.prototype.insertChildAtIndex = function(child, index) {
  if (!index) index = 0
  if (index >= this.children.length) {
    this.appendChild(child)
  } else {
    this.insertBefore(child, this.children[index])
  }
}

Number.prototype.truncate=function(places){
  if(!isset(Math.trunc)) return this;
    return Math.trunc(this * Math.pow(10, places)) / Math.pow(10, places);
};

function base64ToVideoBlob(string){
    var byteCharacters = atob(string);
    var byteNumbers = new Array(byteCharacters.length);
    for (var i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
    }

    var byteArray = new Uint8Array(byteNumbers);
    return new Blob([byteArray], {type: 'video/webm'});
}

function base64ToBlob(b64Data, contentType, sliceSize) {
  contentType = contentType || '';
  sliceSize = sliceSize || 512;

  var byteCharacters = atob(b64Data);
  var byteArrays = [];

  for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
    var slice = byteCharacters.slice(offset, offset + sliceSize);

    var byteNumbers = new Array(slice.length);
    for (var i = 0; i < slice.length; i++) {
      byteNumbers[i] = slice.charCodeAt(i);
    }

    var byteArray = new Uint8Array(byteNumbers);

    byteArrays.push(byteArray);
  }

  var blob = new Blob(byteArrays, {type: contentType});
  return blob;
}

Boolean.prototype.btoa = function() {
    return btoa(unescape(encodeURIComponent(this+"")));
};
Boolean.prototype.atob = function() {
    return decodeURIComponent(escape(atob(this+"")));
};

prependToArray=function(value,array){
  var newArray = array.slice();
  newArray.unshift(value);
  return newArray;
};

Array.prototype.btoa = function() {
    return btoa(unescape(encodeURIComponent(this+"")));
};
Array.prototype.atob = function() {
    return decodeURIComponent(escape(atob(this+"")));
};

Number.prototype.btoa = function() {
    return btoa(unescape(encodeURIComponent(this+"")));
};
Number.prototype.atob = function() {
    return decodeURIComponent(escape(atob(this+"")));
};

String.prototype.btoa = function() {
    return btoa(unescape(encodeURIComponent(this)));
};
String.prototype.atob = function() {
    return decodeURIComponent(escape(atob(this)));
};
String.prototype.capitalize = function() {
    return this.replace(/(?:^|\s)\S/g, function(a) { return a.toUpperCase(); });
};

String.prototype.parseInt=function(){
  return parseInt(this);
};

translate=function(element,string){
  tmp = string.split("/");
  element.innerHTML = vocabulary.page[tmp[0]].phrase[tmp[1]].lang[localStorage.getItem("language")];
};

function sendFile(file, info, ws, progress, done) {
    if(!isset(info)) return;
    if(!isset(ws)) return;
    if(!isset(progress)) progress = function(){};

    var lastResponse;
    var counter = 0;
    var confirmed = false;
    var fileSize   = file.size;
    var chunkSize  = Project.MTU; // bytes
    var offset     = 0;
    var self       = this; // we need a reference to the current object
    var chunkReaderBlock = null;
    var r;
    var blob;
    ws.onmessage=function(e){
      lastResponse = e;
      confirmed = true;
    };
    ws.onclose=function(){
      (done)(lastResponse.data);
    };
    var readEventHandler = function(evt) {
        if (evt.target.error == null) {
            counter++;
            confirmed = false;
            offset += chunkSize;
            if(offset > fileSize) offset = fileSize;
            /*console.log("###################### "+(offset*100/fileSize).truncate(0)+"%");
            console.log("length:"+chunkSize+" (sum:"+offset+", file-size:"+fileSize+")");*/
            (progress)((offset*100/fileSize));
            ws.send(evt.target.result.split("base64,")[1]);
            evt.target.result = null;
            evt = null;
            r = null;
            blob = null;
        } else {
            console.err("Read error: " + evt.target.error);
            return;
        }


          (function poll(){
            if(confirmed){
              if(offset < fileSize){
                chunkReaderBlock(offset, chunkSize, file);
              }else{
                ws.send(";");
              }
            }else{
              setTimeout(function(){poll();},10);
            }
          })();

    }

    chunkReaderBlock = function(_offset, length, _file) {
        r = new FileReader();
        r.onload = readEventHandler;
        blob = _file.slice(_offset, length + _offset);

        r.readAsDataURL(blob);
    }

    //send file information (name,size,ecc)
    var head = JSON.stringify(info).btoa();
    ws.send(head);
    confirmed = false;
    //busy waiting...
    (function poll1(){
      if(confirmed){
        // now separating the header from the body of the file
        ws.send(":");
        confirmed = false;
        //busy waiting again...
        (function poll2(){
          if(confirmed){
            // now let's start the read with the first block
            chunkReaderBlock(offset, chunkSize, file);
          }else{
            setTimeout(function(){poll2();},10);
          }
        })();
      }else{
        setTimeout(function(){poll1();},10);
      }
    })();

}

function Url(string){
    return "url(\""+string+"\")";
}

function Percent(value){
    return value+"%";
}

function Pixel(value){
    return value+"px";
}

function Rgb(red,green,blue){
    return new String("rgb("+red+","+green+","+blue+")");
}

function Rgba(red,green,blue,alfa){
    return new String("rgba("+red+","+green+","+blue+","+alfa+")");
}

function Popup(url,title,i) {

    if(isset(i.toolbar)){
    	if(i.toolbar) i.toolbar = 'yes';
    	if(!i.toolbar) i.toolbar = 'no';
    }else i.toolbar = 'no';

    if(isset(i.location)){
    	if(i.location) i.location = 'yes';
    	if(!i.location) i.location = 'no';
    }else i.location = 'no';

    if(isset(i.directories)){
    	if(i.directories) i.directories = 'yes';
    	if(!i.directories) i.directories = 'no';
    }else i.directories = 'no';

    if(isset(i.status)){
    	if(i.status) i.status = 'yes';
    	if(!i.status) i.status = 'no';
    }else i.status = 'no';

    if(isset(i.menubar)){
    	if(i.menubar) i.menubar = 'yes';
    	if(!i.menubar) i.menubar = 'no';
    }else i.menubar = 'no';

    if(isset(i.scrollbars)){
    	if(i.scrollbars) i.scrollbars = 'yes';
    	if(!i.scrollbars) i.scrollbars = 'no';
    }else i.scrollbars = 'no';

    if(isset(i.resizable)){
    	if(i.resizable) i.resizable = 'yes';
    	if(!i.resizable) i.resizable = 'no';
    }else i.resizable = 'no';

    if(isset(i.copyhistory)){
    	if(i.copyhistory) i.copyhistory = 'yes';
    	if(!i.copyhistory) i.copyhistory = 'no';
    }else i.copyhistory = 'no';

    if(!isset(i.width)) i.width = 800;
    if(!isset(i.height)) i.height = 500;


    i.left = (screen.width/2)-(i.width/2);
    i.top = (screen.height/2)-(i.height/2);

    return window.open(url, title,
    		'toolbar='+i.toolbar+', '+
    		'location='+i.location+', '+
    		'directories='+i.directories+', '+
    		'status='+i.status+', '+
    		'menubar='+i.menubar+', '+
    		'scrollbars='+i.scrollbars+', '+
    		'resizable='+i.resizable+', '+
    		'copyhistory='+i.copyhistory+', '+
    		'width='+i.width+', '+
    		'height='+i.height+', '+
    		'top='+i.top+', '+
    		'left='+i.left+'');
}

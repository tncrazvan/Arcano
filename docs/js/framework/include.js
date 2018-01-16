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

Project.workspace='';
Project.ready = false;

function Project(){}
window.workspace = Project.workspace;

function Includer(){
  var $this = this;
  this.currentModuleRequest = null;
  this.currentCSSRequest = null;
  this.currentJavaScriptRequest = null;
  this.js=function(value){
    return include.js(value,function(file){
      $this.currentJavaScriptRequest = file;
    });
  };
  this.css=function(value){
    return include.css(value,function(file){
      $this.currentCSSRequest = file;
    });
  };
  this.module=function(value){
    return include.module(value,function(mod){
      $this.currentModuleRequest = mod;
    });
  };
};

function include(){}

include.modules = function(list,f){
  f = f || function(){};
  if(list.constructor !== Array){
    return include.modules([list]);
  }else{
    return new Promise(function(resolve,reject){
      let tmpModules = '';
      let i = 0, length = list.length;
      if(length>0){
        (function poll(){
          i++;
          let file = list[i-1]; //without extension
          new HttpEvent(workspace+"modules/"+file+".html",function(result){
            tmpModules += result;
            (f)(file);
            if(i<length){
              poll();
            }else{
              modules.applyHtml(tmpModules,true);
              (resolve)();
            }
          }).run();
        })();
      }
    });
  }
};
include.module = function(list,f){
  return include.modules(list,f);
};

include.css = function(list,f){
  f = f || function(){};
  if(list.constructor !== Array){
    return include.css([list]);
  }else{
    return new Promise(function(resolve,reject){
      let i = 0, length = list.length;
      if(length>0){
        (function poll(){
          i++;
          let file = list[i-1]; //without extension
          let style = document.createElement("link");
          style.setAttribute("rel","stylesheet");
          style.setAttribute("type","text/css");
          if(file.charAt(0)==="@"){
            style.setAttribute("href",(file.replace("@","")));
          }else{
            style.setAttribute("href",workspace+"css/"+file+".css");
          }
          document.head.appendChild(style);
          style.onload=function(){
            (f)(style.getAttribute("href"));
            if(i<length){
              poll();
            }else{
              (resolve)();
            }
          };
        })();
      }
    });
  }
};

include.js = function(list,f){
  f = f || function(){};
  if(list.constructor !== Array){
    return include.js([list]);
  }else{
    return new Promise(function(resolve,reject){
      let i = 0, length = list.length;
      if(list.constructor === Array && length>0){
        (function poll(){
          i++;

          let file = list[i-1]; //without extension
          let script = document.createElement("script");
          script.setAttribute("type","text/javascript");
          script.setAttribute("charset","UTF-8");
          if(file.charAt(0)==="@"){
            script.setAttribute("src",(file.replace("@","")));
          }else{
            script.setAttribute("src",workspace+"js/"+file+".js");
          }
          document.body.appendChild(script);
          script.onload=function(){
            (f)(script.getAttribute("src"));
            if(i<length){
                poll();
            }else{
              (resolve)();
            }
          };
        })();
      }
    });
  }
};

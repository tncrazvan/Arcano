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

function Vocabulary(){
  this.page={};
  this.addPage=function(page){
    this.page[page.name]=page;
  };
}

function Page(name){
  this.name = name;
  this.phrase={};
  this.addPhrase=function(label,content){
    this.phrase[label] = new Phrase(content);
    return this;
  };
};

/*
example:
new Word(
  "it":"contatti",
  "en":"contacts"
);
*/
function Phrase(content){
  this.lang = content;
}

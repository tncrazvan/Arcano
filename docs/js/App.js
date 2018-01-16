window.App=function(uri){
  loader.module([
    "navbar",
    "main"
  ]).then(function(){
    hljs.initHighlightingOnLoad();
  });
};

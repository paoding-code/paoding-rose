/**
 * @author jingwei.li@opi-corp.com
 * @return
 */

function RosePipe() {
	this.loadedWindows = [];
	this.hangedWindows = [];
}
( function RosePipePrototype() {
	var head = document.getElementsByTagName('head')[0];

	var $ = function(id) {
		return document.getElementById(id);
	}

	this.loadCSS = function(href) {
		var ele = document.createElement('link');
		ele.rel = 'stylesheet';
		ele.type = 'text/css';
		ele.href = href;
		head.appendChild(ele);
	}

	this.loadJS = function(href) {
		var ele = document.createElement('script');
		ele.type = 'text/javascript';
		ele.async = true;
		ele.src = href;
		head.appendChild(ele);
	}

	/*
	 * @method addWindow
	 */
	this.addWindow = function(params) {
		var module = new RosePipeWindow(params);

		// 检查依赖模块
		var requiresLoaded = this.checkRequiresLoaded(module);

		if (requiresLoaded)
			this.doAddWindow(module);
		else
			this.hangedWindows.push(module);

		// 本模块加载完毕，重新检查挂起模块
		this.doHangedWindows();
	}

	// 重新检查挂起模块
	this.doHangedWindows = function() {
		for ( var i = 0; i < this.hangedWindows.length; i++) {
			var module = this.hangedWindows[i];
			var requiresLoaded = this.checkRequiresLoaded(module);
			if (requiresLoaded)
				this.doAddWindow(module);
		}
	}

	// 检查依赖模块是否已加载完成
	this.checkRequiresLoaded = function(module) {
		if (!module.requires.length)
			return true;

		var allLoaded = true;
		for ( var i = 0; i < module.requires.length; i++) {
			for ( var j = 0; j < this.loadedWindows.length; j++) {
				if (module.requires[i] == this.loadedWindows[j].id)
					break;
			}
			if (j == this.loadedWindows.length) {
				allLoaded = false;
				break;
			}
		}

		return allLoaded;
	}

	// 添加模块
	this.doAddWindow = function(module) {
		$(module.id).innerHTML = module.html;

		// Load CSS
		if (module.css) {
			for ( var i = 0; i < module.css.length; i++) {
				this.loadCSS(module.css);
			}
		}

		// Load JS
		if (module.js) {
			for ( var i = 0; i < module.js.length; i++) {
				this.loadJS(module.js);
			}
		}

		this.loadedWindows.push(module);
		console.log(module.id + ' loaded')
	}

}).call(RosePipe.prototype);

function RosePipeWindow(params) {
	this.id = params.id || '';
	this.html = params.content || '';
	this.css = params && params.css ? params.css : [];
	this.js = params && params.js ? params.js : [];
	this.requires = params && params.requires ? params.requires : [];
}

var rosepipe = new RosePipe();

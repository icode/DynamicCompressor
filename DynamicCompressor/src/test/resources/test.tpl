<header class="frame-header">
    <div class="frame-logo"><b class="logo-image"></b>

        <div class="logo logo-mask"></div>
    </div>
    <div class="frame-search">
        <input type="search" class="input-search-zoom">
        <button type="button" class="search-go button-blue">Go</button>
    </div>
    <nav class="frame-top">
        <ul>
            <li><a href="/personal" class="username"><%-user.name%></a></li>
            <li id="hadtask"><a href="#" class="usertask">2</a></li>
            <li><a href="task-new.html" class="likebutton newtask button-blue"><b class="newtask-icon"></b>发起任务</a></li>
            <li class="datetask"><a href="#" class="datetaskWrite writetask"><b class="datetaskWrite-icon"></b>填写日志</a>
                <a href="#" class="head-img button-blue"><b class="icon-headimg3"></b></a>

                <div class="datetaskDot"></div>
            </li>
            <li>
                <button type="button" id="logoutBtn" class="exit"><b class="exit-icon"></b></button>
            </li>
        </ul>
    </nav>
</header>
<section class="frame-section">
    <section class="frame-left">
        <nav class="frame-nav">
            <ul>
                <%
                    var menuGroup = _.groupBy(menus,function(menu){return menu.parent==null||menu.parent==-1?"root":menu.parent});
                    var createMenuItem = function(m){
                    _.each(_.sortBy(m,function(m){return m.position}), function(menu){
                %>
                <li navid="<%-menu.id%>"><a href="<%-menu.path?menu.path:\"#\"%>"<%if(menu.description){%> title="<%-menu.description%>"<%}%>><b class="<%-menu.icon%> nav-mask"></b><%-menu.text%></a>
                    <%if(menuGroup[menu.id]){%>
                    <nav class="frame-subnav">
                        <ul>
                            <%createMenuItem(menuGroup[menu.id]+'')%>
                        </ul>
                        <aside class="frame-subnav-point"></aside>
                    </nav>
                    <%}%>
                </li>
                <%})};
                createMenuItem(menuGroup.root)
                %>
            </ul>
            <aside class="frame-nav-point"></aside>
        </nav>
    </section>
    <article class="frame-content">
    </article>
</section>
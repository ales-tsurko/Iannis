IannisGroupView : CompositeView {
  var <name, 
  <contentView, <headerView, 
  nameLabel;

  *new {arg name;
    ^super.new.init(name);
  }

  init {arg groupName;
    name = groupName;

    this.layout = VLayout();
    
    this.initHeader();
    this.initContent();
  }

  initHeader {
    headerView = CompositeView();
    nameLabel = StaticText();
    nameLabel.string = name;

    headerView.fixedHeight = 40;
    headerView.background = Color.gray(0.6);

    headerView.layout = HLayout(nameLabel, nil);

    headerView.mouseDownAction = {arg view;
      this.headerAction(view);
    };

    this.layout.add(headerView);
  }

  initContent {
    contentView = CompositeView();

    this.layout.add(contentView);
  }

  headerAction {arg view;
    contentView.visible = contentView.visible.not;
  }
}

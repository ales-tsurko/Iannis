IannisSynthViewController : CompositeView {
  var <synthName, 
  <pagesView, 
  <node, 
  <metadata, 
  <presetsManagerController,
  <midiInManagerController,
  <data,
  <synthDefName,
  <mapView,
  <selectedElementKey,
  <>midiLearnModeEnabled = true,
  toolbarView, 
  synthNameLabel;

  *new {arg node, synthDefName;
    ^super.new.init(node, synthDefName);
  }

  init {arg aNode, aSynthDefName;
    node = aNode;
    synthDefName = aSynthDefName;
    metadata = SynthDescLib.getLib(\iannis_synth)[synthDefName.asSymbol].metadata;
    data = ();
    this.fixedWidth = 680;
    this.minHeight = 550;
    this.layout = VLayout();
    this.initToolbar();
    mapView = IannisSynthMapPage(this);
    this.parse();

    this.pagesView.addPage("Map", mapView);
    this.onClose = {this.cleanUp()};
  }

  initToolbar {
    toolbarView = CompositeView();
    toolbarView.fixedHeight = 90;

    synthNameLabel = StaticText();
    synthNameLabel.font = Font("Arial", 20);

    presetsManagerController = IannisPresetsManagerController(this);
    midiInManagerController = IannisMIDIInManagerController(this);

    toolbarView.layout = VLayout(
      HLayout(synthNameLabel, nil),
      HLayout(presetsManagerController, nil, midiInManagerController)
    );

    this.layout.add(toolbarView);
  }

  initPagesView {arg name;
    var view = CompositeView();
    view.layout = VLayout(nil);
    pagesView = IannisTabbedView(name, view);

    this.layout.add(pagesView);
  }

  cleanUp {
    midiInManagerController.cleanUp();
    mapView.cleanUp();
    node.free();
  }

  addPage {arg name;
    if (pagesView.isNil) {
      this.initPagesView(name)
    } {
      var view = CompositeView();
      view.layout = VLayout(nil);
      this.pagesView.addPage(name, view);
    };
  }

  getPageViewAtIndex {arg index; 
    ^this.pagesView.views[index].canvas;
  }

  addGroupViewToPageAtIndex {arg groupView, index;
    var newPageView = this.getPageViewAtIndex(index);
    var insertIndex = newPageView.children.size;
    newPageView.layout.insert(groupView, insertIndex);
  }

  switchPage {arg index;
    this.pagesView.switchPage(index);
  }

  synthName_ {arg newName;
    synthName = newName;
    synthNameLabel.string = newName;
  }

  didFinishParsing {
    this.presetsManagerController.parentControllerDidFinishParsing();

    // midi learn related
    this.data.keysDo({arg key;
      var view = this.data[key][\view];
      var previousColor;
      view.canFocus = true;
      
      view.focusGainedAction = {arg v;
        if (this.midiLearnModeEnabled) {
          previousColor = v.background;
          v.background = Color.gray(0.9);
          selectedElementKey = key; 
          this.selectedElementKey.postln;
        };
      };

      view.focusLostAction = {arg v;
        v.background = previousColor?Color.clear();
        selectedElementKey = nil;
        previousColor = nil;
      };
    });
  }
}

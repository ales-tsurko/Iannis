IannisSynthMapParameter : CompositeView {
  var <key, <name, <parentSynthController,
  <nodeProxy,
  nameLabel,
  <textView,
  closeButton,
  evaluateButton,
  onOffButton,
  xfadeNumberBox;
  
  *new {arg key, name, parentSynthController;
    ^super.new.init(key, name, parentSynthController);
  }

  init {arg aKey, aName, aDelegate;
    key = aKey;
    name = aName;
    parentSynthController = aDelegate;

    nodeProxy = NodeProxy(Server.default);

    this.initNameLabel();
    this.initCloseButton();
    this.initEvaluateButton();
    this.initOnOffButton();
    this.initXFadeNumberBox();
    this.initTextView();

    ~xFadeLabel = StaticText();
    ~xFadeLabel.string = "XFade Time:";

    this.layout = VLayout(
      HLayout(
        closeButton, nameLabel, evaluateButton,
        nil,
        ~xFadeLabel, xfadeNumberBox, onOffButton
      ),
      textView
    );
  }

  initNameLabel {
    nameLabel = StaticText();
    nameLabel.string = name;
  }

  initCloseButton {
    closeButton = Button();
    closeButton.fixedWidth = 18;
    closeButton.fixedHeight = 18;
    closeButton.states = [["✖"]];

    closeButton.action = {arg but;
      if (but.value == 0) {
        this.showCloseAlert({
          this.close();
        });
      }
    };
  }

  initEvaluateButton {
    evaluateButton = Button();
    evaluateButton.fixedWidth = 100;
    evaluateButton.states = [["Evaluate"]];

    evaluateButton.action = {arg but;
      if (but.value == 0) {
        textView.getValue({arg codeString;
          this.evaluateCodeAction(codeString);
        });
      } 
    };
  }

  initOnOffButton {
    onOffButton = Button();
    onOffButton.fixedWidth = 30;

    onOffButton.states = [["On"], ["Off"]];

    onOffButton.action = {arg but;
      if (but.value == 1) {
        // off
        // set real/fixed value
      } {
        // on
        // set the modulation again
      };
    };
  }

  initXFadeNumberBox {
    xfadeNumberBox = NumberBox();
    xfadeNumberBox.fixedWidth = 60;
    xfadeNumberBox.clipLo = 0.0;
    xfadeNumberBox.clipHi = 60;

    xfadeNumberBox.action = {arg num;
      nodeProxy.fadeTime = num.value;
    };
  }

  initTextView {
    textView = IannisAceWrapper();

    textView.onLoadFinished = {arg wv;
      wv.setValue(
        "/*\n"
        "Ctrl-R to evaluate the entire document or\n"
        "Shift-Enter to evaluate a line or selection.\n"
        "Ctrl-` - switching between Vim/Normal mode.\n"
        "Ctrl-Alt-H - view all the keyboard shortcuts.\n"
        "*/"
      );
    };

    textView.onEvaluate = {arg code;
      this.evaluateCodeAction(code);
    };

    textView.onEvaluateSelection = {arg code;
      code.interpretPrint;
    };
  }

  evaluateCodeAction {arg code;
    nodeProxy.source = code.compile();
    parentSynthController.node.set(key, nodeProxy.bus.asMap);
  }

  showCloseAlert {arg okCallback;
    var screenBounds = Window.screenBounds();
    var rect = Rect(
      screenBounds.width/2-125,
      screenBounds.height/2-50,
      250,
      100
    );
    var window = Window("Warning", rect, false);
    var message = StaticText();
    var okButton = Button();
    var cancelButton = Button();
    window.alwaysOnTop = true;

    message.string = "This action is undoable. Are you sure you want to remove this block?";
    message.align = \center;

    okButton.fixedWidth = 90;
    okButton.states = [["OK"]];
    okButton.action = {arg but;
      if (but.value == 0) {
        okCallback.value();
        window.close();
      };
    };

    cancelButton.fixedWidth = 90;
    cancelButton.states = [["Cancel"]];
    cancelButton.action = {arg but;
      if (but.value == 0) {
        window.close();
      };
    };

    window.layout = VLayout(
      message,
      HLayout(
        nil,
        cancelButton,
        okButton,
        nil
      )
    );

    window.front();
  }

}

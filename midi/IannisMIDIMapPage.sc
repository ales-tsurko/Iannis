IannisMIDIMapPage : CompositeView {
  var <parentSynthController,
  parameters;

  *new {arg parentSynthController;
    ^super.new.init(parentSynthController);
  }

  init {arg parent;
    var midiControllers = parent.midiInManagerController.midiManager.map[\cc];
    parentSynthController = parent;
    parameters = ();

    this.layout = VLayout();

    midiControllers.keysValuesDo({arg key, obj;
      this.addParameter(
        key, 
        obj[\ccinfo][0], 
        obj[\ccinfo][1], 
        obj[\ccinfo][2]
      );
    });
  }

  addParameter {arg key, sourceUID, ccNum, channel;
    var view = IannisMIDIParameterView(
      this,
      key,
      sourceUID,
      ccNum,
      channel
    );

    parameters[key] = view;

    this.layout.insert(view, this.layout.children.size);
  }

  removeParameter {arg key;
    parameters[key].close();
    parameters[key] = nil;
  }
}






IannisMIDIParameterView : CompositeView {
  var <parent,
  <parameterKey,
  <sourceUID,
  <ccNum,
  <channel,
  closeButton,
  parameterLabel,
  sourcesPopup, 
  sourcesLabel,
  ccNumPopup, 
  ccLabel,
  channelNumberBox, 
  channelLabel;

  *new {arg parent, parameterKey, sourceUID, ccNum, channel;
    ^super.new.init(parent, parameterKey, sourceUID, ccNum, channel);
  }

  init {arg aParent, key, src, cc, chan;
    parent = aParent;
    parameterKey = key;
    sourceUID = src;
    ccNum = cc;
    channel = chan;

    this.initCloseButton();
    this.initParameterLabel();
    this.initSourcesPopup();
    this.initCCNumPopup();
    this.initChannelNumberBox();

    this.layout = HLayout(
      closeButton,
      parameterLabel,
      nil,
      sourcesLabel,
      sourcesPopup,
      nil,
      ccLabel,
      ccNumPopup,
      nil,
      channelLabel,
      channelNumberBox
    );
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

  initParameterLabel {
    parameterLabel = StaticText();
    parameterLabel.string = parameterKey.asString;
  }

  initSourcesPopup {
    var sources = IannisMIDIClient.sources;
    var names = sources.collect(_.name);
    sourcesLabel = StaticText();
    sourcesLabel.string = "Input:";
    sourcesPopup = PopUpMenu();
    sourcesPopup.items = names;

    // FIXME:
    // конфигурация подключеных устройств может измениться,
    // что должно отражаться в меню доступных устройств,
    // иначе при изменении конфигурации, пользователь
    // будет работать с некорректными данными (то есть это
    // потенциальный баг)

    sourcesPopup.action = {arg popup;
      sourceUID = sources[popup.value].uid;
      // change the source
      parent.parentSynthController
      .midiInManagerController
      .midiManager
      .updateMIDIControllerParameters(this.parameterKey, sourceUID);
    };

    // init value
    sourcesPopup.value = IannisMIDIClient.sources.detectIndex({arg d;
      d.uid == this.sourceUID;
    });
  }

  initCCNumPopup {
    var ccs = (0..127);
    ccLabel = StaticText();
    ccLabel.string = "CC Num.:";
    ccNumPopup = PopUpMenu();
    ccNumPopup.items = ccs;

    ccNumPopup.action = {arg popup;
      ccNum = ccs[popup.value];

      // change the CC number
      parent.parentSynthController
      .midiInManagerController
      .midiManager
      .updateMIDIControllerParameters(this.parameterKey, ccNum: ccNum);
    };

    ccNumPopup.value = this.ccNum;
  }

  initChannelNumberBox {
    channelLabel = StaticText();
    channelLabel.string = "Channel:";

    channelNumberBox = NumberBox();
    channelNumberBox.fixedWidth = 60;
    channelNumberBox.clipLo = 1;
    channelNumberBox.clipHi = 16;

    channelNumberBox.action = {arg num;
      channel = num.value - 1;

      // change the channel
      parent.parentSynthController
      .midiInManagerController
      .midiManager
      .updateMIDIControllerParameters(this.parameterKey, channel: channel);
    };

    channelNumberBox.value = this.channel;
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

    message.string = "Are you really want to remove this MIDI binding?";
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

IannisMixerTrackViewController : CompositeView {
  var <trackView,
  <effectsRackView;
  
  *new {arg name;
    ^super.new.init(name);
  }

  init {arg aName;
    trackView = IannisMixerTrackView(aName);

    this.layout = HLayout(
      trackView,
      effectsRackView,
      nil
    );
  }
}

IannisMixerTrackView : CompositeView {
  var instrumentPopup,
  nameLabel,
  gainLabel,
  gainFader,
  peaksLabels,
  levelMeters,
  levelMeterUpdater,
  panLabel,
  panKnob,
  muteButton,
  soloButton,
  <mixerTrack;
  
  *new {arg name;
    ^super.new.init(name);
  }

  init {arg aName;
    mixerTrack = IannisMixerTrack(aName);

    this.initInstrumentPopup();
    this.initGainLabel();
    this.initGainFader();
    this.initPeaksLabels();
    this.initLevelMeters();
    this.initPanLabel();
    this.initPanKnob();
    this.initMuteButton();
    this.initSoloButton();
    this.initNameLabel();

    ~levelMeterLayout = HLayout(
      levelMeters[0],
      levelMeters[1]
    );
    ~levelMeterLayout.spacing = 1;

    ~peaksLabelsLayout = HLayout(
        peaksLabels[0],
        peaksLabels[1],
    );
    ~peaksLabelsLayout.spacing = 1;

    this.layout = VLayout(
      // Instruments Menu
      [instrumentPopup, align: \center],
      HLayout(
        [nil, stretch: 5],
        [~peaksLabelsLayout, stretch: 4],
        [nil, stretch: 2]
      ),
      HLayout(
        nil,
        [gainLabel, align: \topRight, stretch: 1],
        gainFader,
        ~levelMeterLayout,
        [nil, stretch: 1]
      ),
      nil,
      // Pan Knob
      [panLabel, align: \center],
      [panKnob, align: \center],
      nil,
      // Mute and Solo buttons
      HLayout(
        nil,
        muteButton,
        soloButton,
        nil
      ),
      // Track Name
      [nameLabel, align: \center]
    );

    this.fixedWidth = 160;
    this.fixedHeight = 385;

    this.onClose = {
      this.cleanUp();
    };
  }

  initInstrumentPopup {
    instrumentPopup = PopUpMenu();
    instrumentPopup.fixedWidth = 130;
    instrumentPopup.allowsReselection = true;

    instrumentPopup.items = IannisInstrumentsManager.availableInstrumentsNames;

    instrumentPopup.action = {arg popup;
      var keys = IannisInstrumentsManager.availableInstrumentsDescs.keys;
      var selectedKey = keys.asArray[popup.value];

      mixerTrack.instrumentsManager.selectInstrument(selectedKey);
    };
  }

  initGainLabel {
    var font = Font.default;
    font.size = 10;
    gainLabel = StaticText();
    gainLabel.string = mixerTrack.gain.round(0.01);
    gainLabel.font = font;
  }

  initGainFader {
    var gainSpec = ControlSpec(0.ampdb, 6, \db);
    gainFader = Slider();
    gainFader.fixedWidth = 25;
    gainFader.fixedHeight = 200;

    gainFader.action = {arg slider;
      mixerTrack.gain = gainSpec.map(slider.value);

      gainLabel.string = mixerTrack.gain.round(0.01);
    };

    gainFader.value = gainSpec.unmap(mixerTrack.gain);
  }

  initPeaksLabels {
    peaksLabels = nil!2;

    peaksLabels.do({arg item, n;
      var font = Font.default;
      font.size = 10;
      peaksLabels[n] = StaticText();
      peaksLabels[n].string = "-inf";
      peaksLabels[n].font = font;
    });
  }

  initLevelMeters {
    levelMeters = nil!2;

    levelMeters.do({arg item, n;
      var meter = LevelIndicator();
      meter.fixedWidth = 12;
      meter.fixedHeight = 200;
      meter.warning = -3.dbamp;
      meter.critical = -0.03.dbamp;
      meter.drawsPeak = true;
      meter.peakLevel = 0;

      levelMeters[n] = meter;
    });

    this.runLevelMeterUpdater();
  }

  runLevelMeterUpdater {
    levelMeterUpdater??{
      levelMeterUpdater = Routine({
        loop {
          var values = mixerTrack.bus.getnSynchronous(4);
          values = values.abs;

          levelMeters.do({arg meter, n;
              // assign values
              meter.value = values[n];

              // asign peaks
              meter.peakLevel = values[n+2];
              // update peaks labels
              peaksLabels[n].string = values[n+2].ampdb.round(0.1);
          });

          0.1.wait;
        }
      });
    };

    if (levelMeterUpdater.isPlaying.not) {
      AppClock.play(levelMeterUpdater);
    }
  }

  stopLevelMeterUpdater {
    levelMeterUpdater.stop();
    levelMeterUpdater.free();
    levelMeterUpdater = nil;
  }

  initPanLabel {
    var font = Font.default;
    font.size = 10;
    panLabel = StaticText();
    panLabel.string = mixerTrack.pan.round(0.01);
    panLabel.font = font;
  }

  initPanKnob {
    var panSpec = ControlSpec(-1, 1);
    panKnob = Knob();
    panKnob.fixedWidth = 35;
    panKnob.mode = \vert;
    panKnob.centered = true;

    panKnob.action = {arg knob;
      mixerTrack.pan = panSpec.map(knob.value);

      panLabel.string = mixerTrack.pan.round(0.01);
    };

    panKnob.value = panSpec.unmap(mixerTrack.pan);
  }

  initMuteButton {
    muteButton = Button();
    muteButton.fixedWidth = 18;
    muteButton.fixedHeight = 18;
    muteButton.states = [
      ["M", Color.black, Color.white], 
      ["M", Color.white, Color.red]
    ];

    muteButton.action = {arg button;
      mixerTrack.isMute = button.value == 1;
    };

    muteButton.value = mixerTrack.isMute.asInt;
  }

  initSoloButton {
    soloButton = Button();
    soloButton.fixedWidth = 18;
    soloButton.fixedHeight = 18;
    soloButton.states = [
      ["S", Color.black, Color.white], 
      ["S", Color.black, Color.yellow]
    ];

    soloButton.action = {arg button;
      mixerTrack.isSolo = button.value == 1;
    };

    soloButton.value = mixerTrack.isSolo.asInt;
  }

  initNameLabel {
    nameLabel = TextField();
    nameLabel.fixedWidth = 130;
    nameLabel.align = \center;

    nameLabel.action = {arg tf;
      mixerTrack.name = tf.value;
    };

    nameLabel.value = mixerTrack.name;
  }

  cleanUp {
    this.stopLevelMeterUpdater();
    mixerTrack.cleanUp();
  }
}

//
// Effects Rack
//

IannisEffectsRackView : CompositeView {
  var mixerTrack,
  label,
  addSlotButton,
  removeSlotButton,
  rackView,
  <slots;

  *new {arg mixerTrack;
    ^super.new.init(mixerTrack);
  }

  init {arg aMixerTrack;
    mixerTrack = aMixerTrack;
    slots = [];
    label = StaticText();
    label.string = "Effects";

    this.initRackView();
    this.initAddSlotButton();
    this.initRemoveSlotButton();

    this.layout = VLayout(
      [label, align: \center],
      rackView,
      HLayout(
        nil,
        addSlotButton,
        removeSlotButton,
        nil
      ),
    );

    // Appearance
    this.fixedWidth = 160;
    this.layout.margins = 0!4;
  }

  initRackView {
    rackView = ScrollView();
    rackView.hasHorizontalScroller = false;
    rackView.hasBorder = false;
    rackView.fixedWidth = 160;
    rackView.canvas = CompositeView();

    rackView.canvas.layout = VLayout(
      nil,
    );

    rackView.canvas.layout.spacing = 1;
    rackView.canvas.layout.margins = 0!4;
  }

  initAddSlotButton {
    addSlotButton = Button();
    addSlotButton.fixedWidth = 18;
    addSlotButton.fixedHeight = 18;
    addSlotButton.states = [["+"]];

    addSlotButton.action = {arg button;
      var newSlot = IannisEffectSlotView(this);
      slots = slots.add(newSlot);
      rackView.canvas.layout.insert(
        newSlot,
        slots.size-1,
        align: \center
      );
    };
  }

  initRemoveSlotButton {
    removeSlotButton = Button();
    removeSlotButton.fixedWidth = 18;
    removeSlotButton.fixedHeight = 18;
    removeSlotButton.states = [["-"]];
    removeSlotButton.action = {arg button;
      var last = slots.last;
      last!?{
        last.close();
      };

      if (slots.size > 0) {
        slots.removeAt(slots.size-1);
      }
    };
  }

  moveSlotToIndex {arg index, newIndex;
    rackView.canvas.layout.insert(slots[index], newIndex, align: \center);
    slots.move(index, newIndex);
  }
}



IannisEffectSlotView : UserView {
  var bypassButton,
  editButton,
  effectsPopUp,
  dragHandler,
  rackView;

  *new {arg rackView;
    ^super.new.init(rackView);
  }

  init {arg aRackView;
    rackView = aRackView;

    this.initBypassButton();
    this.initEditButton();
    this.initEffectsPopUp();
    this.initDragHandler();

    this.layout = HLayout(
      bypassButton,
      editButton,
      effectsPopUp,
      nil,
      dragHandler
    );

    // Appearance
    this.layout.spacing = 3;
    this.layout.margins = 2!4;
    this.background = Color.gray(0.7);
    this.fixedWidth = 130;
    this.fixedHeight = 23;

    // Drag and drop
    this.initDragAndDrop();
  }

  initBypassButton {
    bypassButton = Button();
    bypassButton.fixedWidth = 18;
    bypassButton.fixedHeight = 18;
    bypassButton.states = [["B"], ["B", Color.white(), Color.red()]];

    bypassButton.action = {arg button;

      // change handler color
      dragHandler!?{
        dragHandler.stringColor = [
          Color.green(), 
          Color.blue()
        ][button.value];
      }
    };
  }

  initEditButton {
    editButton = Button();
    editButton.fixedWidth = 18;
    editButton.fixedHeight = 18;
    editButton.states = [["E"]];
  }

  initEffectsPopUp {
    effectsPopUp = PopUpMenu();
    effectsPopUp.items = ["None"].addAll(
      IannisEffectsManager.availableEffectsNames
    );

    effectsPopUp.action = {arg popup;
    };
  }

  initDragHandler {
    dragHandler = StaticText();
    dragHandler.string = "≡";
    dragHandler.stringColor = Color.green();
    dragHandler.acceptsMouse = false;
  }

  initDragAndDrop {
    this.acceptsMouse = true;
    this.initFocusIndicator();
    this.setDragEventsEnabled(true);

    this.mouseDownAction = {arg ciew, x, y;
      this.beginDrag(x, y);
    };

    this.beginDragAction = {arg view, x, y;
      view.drawingEnabled = false;
      view.refresh();
      this.dragLabel = effectsPopUp.item?"Effect Slot";
      this;
    };

    this.canReceiveDragHandler = {arg view, x, y;
      var canReceive = view != View.currentDrag;
      view.drawingEnabled = canReceive;
      view.refresh();

      rackView.slots.do({arg slot;
        if (slot != view) {
          slot.drawingEnabled = false;
          slot.refresh();
        }
      });

      canReceive;
    };

    this.receiveDragHandler = {arg view, x, y;
      var currentIndex = rackView.slots.indexOf(View.currentDrag);
      var newIndex = rackView.slots.indexOf(view);
      rackView.moveSlotToIndex(currentIndex, newIndex);

      view.drawingEnabled = false;
      view.refresh();
    };
  }

  initFocusIndicator {
    this.drawFunc = {arg view;
      Pen.strokeColor = dragHandler.stringColor?Color.green();
      Pen.width = 2;
      Pen.moveTo(0@0);
      Pen.lineTo(this.bounds.width@0);
      Pen.stroke; 
    };
    this.drawingEnabled = false;
  }
}

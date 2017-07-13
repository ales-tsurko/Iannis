IannisMixerTrackView : CompositeView {
  var instrumentPopup,
  nameLabel,
  gainLabel,
  gainFader,
  levelMeters,
  levelMeterUpdater,
  peaksReseter,
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
    this.initLevelMeters();
    this.initPanLabel();
    this.initPanKnob();
    this.initMuteButton();
    this.initSoloButton();
    this.initNameLabel();

    this.layout = VLayout(
      [instrumentPopup, align: \center],
      [gainLabel, align: \center],
      HLayout(
        nil,
        gainFader,
        levelMeters[0],
        levelMeters[1],
        nil
      ),
      [panLabel, align: \center],
      [panKnob, align: \center],
      HLayout(
        muteButton,
        soloButton
      ),
      [nameLabel, align: \center]
    );

    this.onClose = {
      this.cleanUp();
    };
  }

  initInstrumentPopup {
    instrumentPopup = PopUpMenu();
    instrumentPopup.fixedWidth = 100;
    instrumentPopup.allowsReselection = true;

    instrumentPopup.items = mixerTrack.instrumentsManager.availableInstrumentsNames;

    instrumentPopup.action = {arg popup;
      var keys = IannisInstrumentsManager.availableInstrumentsDescs.keys;
      var selectedKey = keys.asArray[popup.value];

      mixerTrack.instrumentsManager.selectInstrument(selectedKey);
    };
  }

  initGainLabel {
    gainLabel = StaticText();
    gainLabel.string = mixerTrack.gain.round(0.01);
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

  initLevelMeters {
    levelMeters = nil!2;

    levelMeters.do({arg item, n;
      var meter = LevelIndicator();
      meter.fixedWidth = 13;
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
    var peaks = 0!2;
    levelMeterUpdater??{
      levelMeterUpdater = Routine({
        loop {
          var values = mixerTrack.bus.getnSynchronous(2);
          values[0] = values[0].abs;
          values[1] = values[1].abs;

          levelMeters.do({arg meter, n;
            meter.value = values[n];

            if (values[n] > peaks[n]) {
              peaks[n] = values[n];
            };

            meter.peakLevel = peaks[n];
          });

          0.1.wait;
        }
      });
    };

    peaksReseter??{
      peaksReseter = Routine({
        loop {
          levelMeters.do({arg meter, n;
            peaks = 0!2;
            meter.peakLevel = 0;
          });

          5.wait;
        }
      });
    };

    if (levelMeterUpdater.isPlaying.not) {
      AppClock.play(levelMeterUpdater);
      AppClock.play(peaksReseter);
    }
  }

  stopLevelMeterUpdater {
    levelMeterUpdater.stop();
    levelMeterUpdater.free();
    levelMeterUpdater = nil;

    peaksReseter.stop();
    peaksReseter.free();
    peaksReseter = nil;
  }

  initPanLabel {
    panLabel = StaticText();
    panLabel.string = mixerTrack.pan.round(0.01);
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
    nameLabel.fixedWidth = 100;
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

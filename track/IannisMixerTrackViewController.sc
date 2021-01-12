// View controller
IannisMixerTrackViewController : CompositeView {
    var busLabel,
    toggleRackButton,
    instrumentPopup,
    editInstrumentButton,
    effectsRackView,
    <nameLabel,
    gainLabel,
    gainFader,
    peaksLabels,
    levelMeters,
    levelMeterUpdater,
    panLabel,
    panKnob,
    toggleMIDIInputButton,
    muteButton,
    soloButton,
    <viewControllersRack,
    <mixerTrack,
    <isMaster,
    <innerBus,
    <delegate,
    <index,
    hasInstrumentChooser;

    *new {arg name, isMaster = false, delegate, index, hasInstrumentChooser = true;
        ^super.new.init(name, isMaster, delegate, index, hasInstrumentChooser);
    }

    init {arg aName, isMas, del, n, ahasInstrumentChooser;
        isMaster = isMas;
        delegate = del;
        index = n;
        hasInstrumentChooser = ahasInstrumentChooser;

        IannisTabbedView.isScrollable = false;
        innerBus = Bus(\audio, 0, 2, Server.default);
        if(isMaster.not){innerBus = Bus.audio(Server.default, 2)};
        mixerTrack = IannisMixerTrack(aName, isMaster, innerBus);
        viewControllersRack = IannisViewControllersRack(isMaster, hasInstrumentChooser);
        viewControllersRack.visible = false;

        this.initBusLabel();
        this.initToggleRackButton();
        if(isMas.not) {
            this.initInstrumentPopup();
            this.initEditInstrumentButton();
        };
        this.initEffectsRackView();
        this.initGainLabel();
        this.initGainFader();
        this.initPeaksLabels();
        this.initLevelMeters();
        this.initPanLabel();
        this.initPanKnob();
        if(isMaster.not && hasInstrumentChooser) {
            this.initToggleMIDIInputButton();
        };
        this.initMuteButton();
        if(isMas.not) {
            this.initSoloButton();
        };
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
        
        ~headerElements = [];
        if(isMaster){
            ~headerElements = [
                busLabel,
                [toggleRackButton, align: \center],
                [effectsRackView, align: \top]
            ];
        }{
			if (hasInstrumentChooser) {
				~headerElements = [
					busLabel,
					[toggleRackButton, align: \center],
					// Instruments Menu
					// HLayout(
					// nil,
					// editInstrumentButton,
					[instrumentPopup, align: \center],
					// nil
					// ),
					[effectsRackView, align: \top]
				];

			} {
				~headerElements = [
					busLabel,
					[toggleRackButton, align: \center],
					// Instruments Menu
					// HLayout(
					// nil,
					// editInstrumentButton,
					// nil
					// ),
					[effectsRackView, align: \top]
				];
			}
		};

        ~trackLayout = VLayout(
            VLayout(*~headerElements),
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
            // Pan Knob
            [panLabel, align: \bottom],
            [panKnob, align: \bottom],
            // Mute and Solo buttons
            HLayout(
                nil,
                toggleMIDIInputButton,
                muteButton,
                soloButton,
                nil
            ),
            // Track Name
            [nameLabel, align: \center]
        );

        this.layout = HLayout(
            ~trackLayout,
            viewControllersRack
        );

        this.minHeight = 620;

        this.onClose = {
            this.cleanUp();
        };
    }

    initBusLabel {
        busLabel = StaticText();
        busLabel.string = "Bus:" + innerBus.index;
        busLabel.align = \center;
    }

    initToggleRackButton {
        toggleRackButton = Button();
        toggleRackButton.fixedWidth = 130;
        toggleRackButton.states = [["Edit"], ["Compact"]];

        toggleRackButton.action = {arg button;
            viewControllersRack.visible = button.value.asBoolean;
        };
    }

    initInstrumentPopup {
        instrumentPopup = PopUpMenu();
        instrumentPopup.fixedWidth = 130;

        instrumentPopup.items = ["LiveCode"].addAll(
            IannisInstrumentsManager.availableInstrumentsNames
        );

        instrumentPopup.action = {arg popup;
            mixerTrack.instrumentsManager.selectInstrument(popup.value-1);

            // update view in rack
            viewControllersRack.instrumentViewController = mixerTrack
            .instrumentsManager
            .synthViewController;
        };

        instrumentPopup.valueAction = 0;
    }

    initEditInstrumentButton {
        editInstrumentButton = Button();
        editInstrumentButton.fixedWidth = 18;
        editInstrumentButton.fixedHeight = 18;
        editInstrumentButton.states = [["E"]];

        editInstrumentButton.action = {
            mixerTrack.instrumentsManager.synthViewController.front();
        };
    }

    initEffectsRackView {
        effectsRackView = IannisEffectsRackView(this);
        effectsRackView.fixedHeight = 120;
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
        gainFader.minHeight = 200;

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
            meter.minHeight = 200;
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

    initToggleMIDIInputButton {
        toggleMIDIInputButton = Button();
        toggleMIDIInputButton.fixedWidth = 23;
        toggleMIDIInputButton.fixedHeight = 23;
        toggleMIDIInputButton.states = [["🎹"], ["🎹", Color.black(), Color.green()]];

        toggleMIDIInputButton.action = {arg button;
            var newValue = button.value.asBoolean;

            // toggle instrument input
            mixerTrack
            .instrumentsManager
            .synthViewController
            .midiView
            .midiManager
            .midiInputEnabled = newValue;

            // toggle effects input
            mixerTrack
            .effectsManager
            .effectsViewControllers.do({arg viewController;
                viewController
                .midiView
                .midiManager
                .midiInputEnabled = newValue;
            });
        }
    }

    initMuteButton {
        muteButton = Button();
        muteButton.fixedWidth = 23;
        muteButton.fixedHeight = 23;
        muteButton.states = [
            ["M", Color.black, Color.white], 
            ["M", Color.white, Color.red]
        ];

        muteButton.action = {arg button;
            var mute = button.value == 1;
            mixerTrack.isMute = mute;
            delegate.didToggleMuteAtChannel(this, mute);
        };

        muteButton.value = mixerTrack.isMute.asInteger;
    }

    setMute {arg value;
        muteButton.value = value.asInteger;
        mixerTrack.isMute = value;
    }

    initSoloButton {
        soloButton = Button();
        soloButton.fixedWidth = 23;
        soloButton.fixedHeight = 23;
        soloButton.states = [
            ["S", Color.black, Color.white], 
            ["S", Color.black, Color.yellow]
        ];

        soloButton.action = {arg button;
            var solo = button.value == 1;
            mixerTrack.isSolo = solo;
            delegate.didToggleSoloAtChannel(this, solo);
        };

        soloButton.value = mixerTrack.isSolo.asInteger;
    }
    
    setSolo {arg value;
        soloButton.value = value.asInteger;
        mixerTrack.isSolo = value;
    }

    initNameLabel {
        nameLabel = TextField();
        nameLabel.fixedWidth = 130;
        nameLabel.align = \center;
        if(isMaster){nameLabel.enabled = false};

        nameLabel.action = {arg tf;
            mixerTrack.name = tf.value;
        };

        nameLabel.value = mixerTrack.name;
    }

    cleanUp {
        this.stopLevelMeterUpdater();
        if(isMaster){
            this.viewControllersRack.instrumentViewController.kill();
        };
        mixerTrack.cleanUp();
    }
}


//
// View controllers rack
//
IannisViewControllersRack : ScrollView {
    var <instrumentViewController,
    <effectsViewControllers,
    <isMaster,
    hasInstrumentChooser;

    *new {arg isMaster, hasInstrumentChooser;
        ^super.new.init(isMaster, hasInstrumentChooser)
    }

    init {arg isMas, ahasInstrumentChooser;
        isMaster = isMas;
        hasInstrumentChooser = ahasInstrumentChooser;

        effectsViewControllers = [];

        this.canvas = CompositeView();
        this.initLayout();

        this.fixedWidth = 685;
    }

    initLayout {
        if(isMaster) {
            instrumentViewController = FreqScopeView(this.canvas, Rect(0,0,511,300));
            instrumentViewController.active = true;
            instrumentViewController.freqMode = 1;
            instrumentViewController.dbRange = 180;
        };

        this.canvas.layout = VLayout(
            instrumentViewController,
            *effectsViewControllers
        );
        this.canvas.layout.add(nil);

        this.canvas.layout.spacing = 0;
        this.canvas.layout.margins = 0!4;
    }

    effectsViewControllers_ {arg newValue;
        effectsViewControllers = newValue;

        this.initLayout();
    }

    instrumentViewController_ {arg newValue;
        if(isMaster.not && hasInstrumentChooser) {
            instrumentViewController = newValue;

            this.initLayout();
        }
    }
}


//
// Effects Rack
//

IannisEffectsRackView : CompositeView {
    var <parent,
    label,
    addSlotButton,
    removeSlotButton,
    rackView,
    <slots;

    *new {arg parent;
        ^super.new.init(parent);
    }

    init {arg aParent;
        parent = aParent;
        slots = [];
        label = StaticText();
        label.string = "Effects";

        this.initRackView();
        this.initAddSlotButton();
        this.initRemoveSlotButton();

        this.layout = VLayout(
            [label, align: \top],
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
            nil
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
            var newIndex = slots.size;
            var newSlot = IannisEffectSlotView(this, newIndex);

            slots = slots.add(newSlot);

            rackView.canvas.layout.insert(
                newSlot,
                slots.size-1,
                align: \center
            );

            // model
            parent
            .mixerTrack
            .effectsManager
            .addEffect();

            // update view controllers rack
            this.updateViewControllersRackEffects();
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

                parent
                .mixerTrack
                .effectsManager
                .removeEffectAtIndex(slots.size-1);

                slots.removeAt(slots.size-1);

                // update view controllers rack
                this.updateViewControllersRackEffects();
            };
        };
    }

    moveSlotToIndex {arg index, newIndex;
        rackView.canvas.layout.insert(slots[index], newIndex, align: \center);

        parent
        .mixerTrack
        .effectsManager
        .moveEffectToIndex(index, newIndex);

        slots.move(index, newIndex);

        // update indexes
        slots.do({arg view, n;
            view.index = n;
        });

        // update view controllers rack
        this.updateViewControllersRackEffects();
    }

    updateViewControllersRackEffects {
        parent.viewControllersRack.effectsViewControllers = parent
        .mixerTrack
        .effectsManager
        .effectsViewControllers;
    }
}


IannisEffectSlotView : UserView {
    var bypassButton,
    editButton,
    effectsPopUp,
    dragHandler,
    rackView,
    <>index;

    *new {arg rackView, index;
        ^super.new.init(rackView, index);
    }

    init {arg aRackView, anIndex;
        rackView = aRackView;
        index = anIndex;

        this.initBypassButton();
        this.initEditButton();
        this.initEffectsPopUp();
        this.initDragHandler();

        this.layout = HLayout(
            bypassButton,
            // editButton,
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

        // model
    }

    initBypassButton {
        bypassButton = Button();
        bypassButton.fixedWidth = 18;
        bypassButton.fixedHeight = 18;
        bypassButton.states = [["B"], ["B", Color.white(), Color.red()]];

        bypassButton.action = {arg button;
            var effectViewController = rackView
            .parent
            .mixerTrack
            .effectsManager
            .effectsViewControllers[this.index];

            effectViewController!?{
                effectViewController.isBypassed = button.value.asBoolean;
            };

            // change handler color
            dragHandler!?{
                dragHandler.stringColor = [
                    Color.green(), 
                    Color.blue()
                ][button.value];
            }
        };

        this.bindBypassStateWithButton();
    }

    initEditButton {
        editButton = Button();
        editButton.fixedWidth = 18;
        editButton.fixedHeight = 18;
        editButton.states = [["E"]];

        editButton.action = {
            var effectViewController = rackView
            .parent
            .mixerTrack
            .effectsManager
            .effectsViewControllers[this.index];

            effectViewController!?{
                effectViewController.front();
            }
        };
    }

    initEffectsPopUp {
        effectsPopUp = PopUpMenu();
        effectsPopUp.items = ["LiveCode"].addAll(
            IannisEffectsManager.availableEffectsNames
        );

        effectsPopUp.action = {arg popup;
            var effectName;

            if (popup.value > 0) {
                effectName = IannisEffectsManager
                .availableEffectsDescs[popup.value-1].name
            };

            // update effect
            rackView
            .parent
            .mixerTrack
            .effectsManager
            .changeEffectAtIndex(
                this.index,
                effectName
            );

            // update view controllers rack
            rackView.updateViewControllersRackEffects();

            // bind bypass and slot's button
            this.bindBypassStateWithButton();
        };
    }

    bindBypassStateWithButton {
        var viewController = rackView
        .parent
        .mixerTrack
        .effectsManager
        .effectsViewControllers[this.index];

        viewController!?{
            viewController.onBypass = {arg value;
                bypassButton.value = value.asInteger;
            };
        }
    }

    initDragHandler {
        dragHandler = StaticText();
        dragHandler.string = "≡";
        dragHandler.stringColor = Color.green();
        dragHandler.acceptsMouse = false;
    }

    initDragAndDrop {
        var isCleaned = true;
        this.acceptsMouse = true;
        this.initFocusIndicator();

        this.mouseDownAction = {arg ciew, x, y;
            this.beginDrag(x, y);
        };

        this.beginDragAction = {arg view, x, y;
            view.drawingEnabled = false;
            view.refresh();
            this.dragLabel = effectsPopUp.item?"Effect Slot";
            this;
        };

        this.mouseLeaveAction = {
            if (isCleaned.not) {
                rackView.slots.do({arg slot;
                    slot.drawingEnabled = false;
                    slot.refresh();
                });

                isCleaned = true;
            }
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

            isCleaned = false;

            canReceive;
        };

        this.receiveDragHandler = {arg view, x, y;
            rackView.moveSlotToIndex(View.currentDrag.index, view.index);

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


IannisLiveBoxLaunchpadS {
    var loggerTextFields,
    controlSynth,
    deviceID,
    state,
    midiOut,
    animations,
    currentMode,
    midiFuncs,
    controlsMap,
    isCurrentLayoutDrumRack = true,
    currentPageIndex = 0,
    isModeStepSequencer = false,
    isModeStepSequencerEdit = false,
    pitchersBusses,
    recordingDirectory;

	*new {arg loggerTextFields, controlSynth, pitchersBusses, recordingDirectory;
		^super.new.init(loggerTextFields, controlSynth, pitchersBusses, recordingDirectory);
	}

	init {arg aloggerTextFields, acontrolSynth, apitchersBusses, arecordingDirectory;
		controlSynth = acontrolSynth;
		loggerTextFields = aloggerTextFields;
        pitchersBusses = apitchersBusses;
        recordingDirectory = arecordingDirectory;

		if (this.initDevice()) {
			this.onDeviceInitSuccess();
		} {
			this.initLoggerTextFields();
			loggerTextFields.do({arg tf;
				tf.string = "Launchpad doesn't initialized";
			});
		};
	}

	initDevice {
		var midiIn;
		MIDIClient.init;
		MIDIIn.connectAll;
		midiIn = MIDIIn.findPort("Launchpad S", "Launchpad S");
		if (midiIn.isNil) {
			^false;
		} {
			deviceID = midiIn.uid;
            this.initMIDIOut();
			^true;
		};
	}

    initMIDIOut {
        midiOut = MIDIOut.newByName("Launchpad S", "Launchpad S");
    }

    initState {
        state = ()!2;
        2.do({arg n;
            state[n].isArm = false;
            state[n].samplePlaybackDirection = 1; // 1 - forward, -1 - backward
            state[n].nnToggle = 0!128;
            state[n].activeNNs = [];
            state[n].isOctRandomised = [false,false]; // up,down
            state[n].selectedPadToRecordInto = nil;
            state[n].pitches = 1!128;
            state[n].sequencer = ();
            state[n].sequencer.totalNumberOfSteps = 120;
            state[n].sequencer.numberOfSteps = 64;
            state[n].sequencer.availableStepDurations = [1/4, 1/8, 1/16, 1/32, 1/64] * 4;
            state[n].sequencer.selectedStepDuration = state[n].sequencer.availableStepDurations[2];
        });
    }

    initControlsMap {
        var gridFirstHalf, gridSecondHalf;
        controlsMap = ();
        controlsMap.topButtonsCCs = (104..111);
        controlsMap.rightButtonsNNs = ();
        controlsMap.rightButtonsNNs.drumRackMode = (100..107);
        controlsMap.rightButtonsNNs.xyMode = [];
        8.do({arg n;
            var nn = (n*2+1)*8;
            controlsMap.rightButtonsNNs.xyMode = controlsMap.rightButtonsNNs.xyMode.add(nn);
        });

        controlsMap.mainGridNNs = ();
        controlsMap.mainGridNNs.xyMode = [];
        8.do({arg row;
            var startNote = row*16;
            var notes = (startNote..(startNote+7));
            controlsMap.mainGridNNs.xyMode = controlsMap.mainGridNNs.xyMode++notes;
        });

        gridFirstHalf = (36..67).reshape(8,4).reverse;
        gridSecondHalf = (68..99).reshape(8,4).reverse;
        controlsMap.mainGridNNs.drumRackMode = ([gridFirstHalf].add(gridSecondHalf)).lace(16).flat();
    }

    getNNForGridButtonAtIndex {arg index;
        if (isCurrentLayoutDrumRack) {
            ^controlsMap.mainGridNNs.drumRackMode[index];
        } {
            ^controlsMap.mainGridNNs.xyMode[index];
        };
    }

    getNNForRightFunctionButtonAtIndex {arg index;
        if (isCurrentLayoutDrumRack) {
            ^controlsMap.rightButtonsNNs.drumRackMode[index];
        } {
            ^controlsMap.rightButtonsNNs.xyMode[index];
        };
    }

    initAnimations {
        animations = ();

        this.initArmAnimations();
    }

    initArmAnimations {
        animations.arm = Task({
            loop {
                midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 15);
                0.85.wait;
                midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 16);
                0.15.wait;
            };
        });
    }

    initMIDIFuncs {
        midiFuncs = [];
        midiFuncs = midiFuncs++[this.initArmMIDIFunc()];
        midiFuncs = midiFuncs++[this.initTopButtonsMIDIFunc()];
        midiFuncs = midiFuncs++[this.initMainGridMIDIFunc()];
    }

    initArmMIDIFunc {
        ^MIDIFunc.noteOn({arg vel, note, ch, id;
            if (note == controlsMap.rightButtonsNNs.drumRackMode[7]) {
                this.toggleArmForSynthIndex(currentPageIndex);
            };
        }, srcID: deviceID);
    }

    initTopButtonsMIDIFunc {
        ^MIDIFunc.cc({arg val, num, ch, id;
            if (val == 0) {
                if ((num == controlsMap.topButtonsCCs[5]).or(num == controlsMap.topButtonsCCs[6])) {
                    var pageIndex = (num == controlsMap.topButtonsCCs[6]).asInteger;
                    this.switchPageTo(pageIndex);
                };

            } {
                if ((num == controlsMap.topButtonsCCs[0])) {
                    this.toggleOctaveRandomizationForDirection(0);
                };

                if ((num == controlsMap.topButtonsCCs[1])) {
                    this.toggleOctaveRandomizationForDirection(1);
                };

                if ((num == controlsMap.topButtonsCCs[2]).or(num == controlsMap.topButtonsCCs[3])) {
                    var playbackDirection = if (num == controlsMap.topButtonsCCs[2]) {-1} {1};
                    this.setSamplePlaybackDirection(playbackDirection);
                };


            };

        }, srcID: deviceID);
    }

    switchPageTo {arg index;
        currentPageIndex = index;

        if (state[currentPageIndex].isArm) {
            animations.arm.play();
        } {
            animations.arm.stop();
        };

        midiOut.control(0, 0, 2);
        isCurrentLayoutDrumRack = true;
        isModeStepSequencer = false;
        isModeStepSequencerEdit = false;

        7.do({arg n;
            var buttonNN = this.getNNForRightFunctionButtonAtIndex(n);

            if ((currentPageIndex == 1).and(n < 2)) {
                midiOut.noteOn(0, buttonNN, 30);
            } {
                midiOut.noteOn(0, buttonNN, 29);
            };
        });

        state[1].sequencer.availableStepDurations.do({arg i, n;
            if (i == state[1].sequencer.selectedStepDuration) {midiOut.noteOn(0, 102 + n, 63)};
        });
        midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 16);

        this.resetDrumRackGridLedsToDefault();

        state[currentPageIndex].activeNNs.do({arg n;
            midiOut.noteOn(0, n, 48);
        });

        2.do({arg n;
            state[1].isOctRandomised[n] = state[1].isOctRandomised[n].not;
            this.toggleOctaveRandomizationForDirection(n);
        });

        this.setSamplePlaybackDirection(state[currentPageIndex].samplePlaybackDirection);
    }

    resetDrumRackGridLedsToDefault {
        4.do({arg n;
            4.do({arg i; 
                var index = 8*n+i;
                [0,4,32,36].do({arg a;
                    var na = index + a;
                    midiOut.noteOn(0, this.getNNForGridButtonAtIndex(na), this.getDefaultColorForDrumRackGridNN(na));
                });
            });
        });
    }

    toggleArmForSynthIndex {arg index;
        if (state[index].isArm) {
            midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 16);
            animations.arm.stop();
        } {
            animations.arm.play();
        };

        state[index].isArm = state[index].isArm.not;
    }

    setSamplePlaybackDirection {arg direction;
        var key = if (currentPageIndex == 0) {\direction1} {\direction2};
        var color1 = 63,
        color2 = 19;
        state[currentPageIndex].samplePlaybackDirection = direction;
        if(state[currentPageIndex].samplePlaybackDirection == 1) {
            color1 = 19;
            color2 = 63;
        };
        midiOut.control(0, controlsMap.topButtonsCCs[2], color1);
        midiOut.control(0, controlsMap.topButtonsCCs[3], color2);

        controlSynth.set(key, state[currentPageIndex].samplePlaybackDirection);

        this.logString("Play direction is:"+direction);
    }

    logString {arg value;
        AppClock.sched(0.0, {
            loggerTextFields[currentPageIndex].string = value;
        });
    }

    toggleOctaveRandomizationForDirection {arg direction; 
        // direction = 0 (up), 1 (down)
        var directionString;
        if (state[1].isOctRandomised[direction]) {
            midiOut.control(0, controlsMap.topButtonsCCs[direction], 19);
            controlSynth.set(\roct2, 1);
        } {
            midiOut.control(0, controlsMap.topButtonsCCs[direction], 63);
        };

        state[1].isOctRandomised[direction] = state[1].isOctRandomised[direction].not;

        //log
        directionString = if ( direction == 0 ) {"up"} {"down"};
        this.logString("ROct."+directionString+state[1].isOctRandomised[direction]);
    }

    setRecordingDirectory {arg newValue;
        recordingDirectory = newValue;
    }

    initMainGridMIDIFunc {
        var fileName = nil!2, buffer = nil!2, recorder = nil!2, pitcher = nil!2;

        ^MIDIFunc.noteOn({arg vel, note, ch, id;
            if (controlsMap.mainGridNNs.drumRackMode.includes(note)) {
                if (state[currentPageIndex].isArm) {
                    if ((state[currentPageIndex].selectedPadToRecordInto.isNil).and((state[currentPageIndex].activeNNs.size < 4).or(state[currentPageIndex].activeNNs.includes(note)))) {
                        fileName[currentPageIndex] = recordingDirectory +/+ ("Recording-" ++ thisThread.seconds.asString).replace(".", "-") ++ ".wav";
                        buffer[currentPageIndex] = Buffer.alloc(Server.default, 65536, 1);
                        buffer[currentPageIndex].write(fileName[currentPageIndex], "wav", "int16", 0, 0, true);
                        recorder[currentPageIndex] = Synth.tail(nil, "recorder", ["bufnum", buffer[currentPageIndex]]);
                        pitcher[currentPageIndex] = Synth("pitchtector");
                        midiOut.noteOn(0, note, 15);
                        state[currentPageIndex].selectedPadToRecordInto = note;
                    };
                } {
                    if (state[currentPageIndex].activeNNs.size < 4) {

                        if (state[currentPageIndex].activeNNs.includes(note)) {
                            var index = state[currentPageIndex].activeNNs.indexOf(note);
                            state[currentPageIndex].activeNNs.removeAt(index);

                            midiOut.noteOn(0, note, this.getDefaultColorForDrumRackGridNN(note));
                        } {
                            midiOut.noteOn(0, note, 48);
                            state[currentPageIndex].activeNNs = state[currentPageIndex].activeNNs.add(note);
                        };
                    }
                    {
                        if(state[currentPageIndex].activeNNs.includes(note)) {
                            var index = state[currentPageIndex].activeNNs.indexOf(note);
                            state[currentPageIndex].activeNNs.removeAt(index);
                            midiOut.noteOn(0, note, this.getDefaultColorForDrumRackGridNN(note));
                        };
                    };
                };
            };

            fork {
                if ((state[currentPageIndex].selectedPadToRecordInto.notNil).and(state[currentPageIndex].isArm.not)) {

                    var nn = state[currentPageIndex].selectedPadToRecordInto;
                    state[currentPageIndex].activeNNs = state[currentPageIndex].activeNNs.add(nn);
                    ~samples[currentPageIndex][nn] = Buffer.read(Server.default, fileName[currentPageIndex]);
                    pitchersBusses[currentPageIndex].get({arg v; state[currentPageIndex].pitches[nn] = v});

                    Server.default.sync;

                    midiOut.noteOn(0, nn, 48);

                    state[currentPageIndex].selectedPadToRecordInto = nil;
                    recorder[currentPageIndex].free;
                    pitcher[currentPageIndex].free;
                    buffer[currentPageIndex].close;
                    buffer[currentPageIndex].free;
                    fileName[currentPageIndex].free;

                };

                4.do({arg n;
                    var sampleNumber = state[currentPageIndex].activeNNs.wrapAt(n)?0;
                    var keyIndex = n+1+(4*currentPageIndex);
                    var bufferKey = (\b++keyIndex).asSymbol;
                    var pitcherKey = (\p++keyIndex).asSymbol;
                    var st = (\st++keyIndex).asSymbol;

                    controlSynth.set(bufferKey, ~samples[currentPageIndex][sampleNumber].bufnum);
                    controlSynth.set(pitcherKey, state[currentPageIndex].pitches[sampleNumber]);
                    if(this.isNNInUnpitchedCategory(sampleNumber), {controlSynth.set(st, 0)}, {controlSynth.set(st, 1)});
                });
            };
        }, srcID: deviceID, argTemplate: { (isModeStepSequencer.not).and(isModeStepSequencerEdit.not) });
    }

    isNNInUnpitchedCategory {arg nn;
        ^(33..48).includes(nn);
    }

    getDefaultColorForDrumRackGridNN {arg nn;
        var color;
        case
        {((nn >= 36).and(nn <= 51)).or((nn >= 84).and(nn <= 99))} {
            color = 16;
        }
        { (nn >= 52).and(nn <= 67) } {
            color = 17;
        }
        { (nn >= 68).and(nn <= 83) } {
            color = 1;
        };

        ^color;
    }

    redrawDrumRackGrid {
        midiOut.control(0, 0, 2);
        isCurrentLayoutDrumRack = true;
        midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(0), 30);
        midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(1), 30);

        if (state[1].isArm) {
            animations.arm.play;
            this.resetDrumRackGridLedsToDefault();
            midiOut.noteOn(0, state[currentPageIndex].selectedPadToRecordInto, 15);
            state[1].nnToggle.do({arg item;
                if (item != state[currentPageIndex].selectedPadToRecordInto) {
                    midiOut.noteOn(0, item, 48);
                };
            });
        } {
            animations.arm.stop;
            midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 16);
            this.resetDrumRackGridLedsToDefault();
            state[1].nnToggle.do({arg item;
                midiOut.noteOn(0, item, 48);
            });
        };

        isModeStepSequencer = false;
        isModeStepSequencerEdit = false;
    }

	onDeviceInitSuccess {
        this.initControlsMap();
        this.initState();
        this.initAnimations();
        this.initMIDIFuncs();



        // ------------------------------------------------------------------------- SEQUENCER

        ~modebtns = Array.newClear(8).seriesFill(8, 16).collect({arg n; n + (0..7)}).flat;
        ~sequence = Array.fill(127, 0);
        ~seqRoutInternal = Array.newClear(127);
        ~seqRoutInternalFiltered = ~seqRoutInternal.as(Array);
        ~sequenceToSynth = Array.newClear(127);
        ~seqCursor = (0..127);
        ~seqCursorFiltered = ~seqCursor.as(Array);
        ~routineStep = 0;

        ~seqLedFeedback = {
            ~sequenceToSynth = ~sequence.as(Array);
            ~modebtns.do({arg i, n;
                if(~sequenceToSynth.size > (i - n), {~sequenceToSynth.removeAt(i - n)});
            });

            ~seqRoutInternalFiltered = ~seqRoutInternal.as(Array);
            ~modebtns.do({arg i, n;
                if(~seqRoutInternalFiltered.size > (i - n), {~seqRoutInternalFiltered.removeAt(i - n)});
            });

            ~seqCursorFiltered = ~seqRoutInternal.as(Array);
            ~seqCursorFiltered.do({arg i, n;
                if(i.isNumber, {~seqCursorFiltered[n] = nil},
                {~seqCursorFiltered[n] = n});
            });

            ~modebtns.do({arg i, n;
                if(~seqCursorFiltered.size > (i - n), {~seqCursorFiltered.removeAt(i - n)});
            });
        };

        ~seqLedFeedback.value;

        ~r1 = Routine.run({
            loop{
                ~sequenceToSynth.lace(state[1].sequencer.numberOfSteps).rotate(~routineStep).do({arg i, n;
                    if(i == 1, {controlSynth.set(\trigger, 1000.rand)});
                    ~routineStepExchange = n + 1;
                    if((state[1].isOctRandomised[0]).and(state[1].isOctRandomised[1].not),
                    {controlSynth.set(\roct2, [1, 2, 4].choose)});
                    if((state[1].isOctRandomised[1]).and(state[1].isOctRandomised[0].not),
                    {controlSynth.set(\roct2, [0.25, 0.5, 1].choose)});
                    if((state[1].isOctRandomised[0]).and(state[1].isOctRandomised[1]),
                    {controlSynth.set(\roct2, [0.25, 0.5, 1, 2, 4].choose)});

                    state[1].sequencer.selectedStepDuration.wait;
                });
            };
        });

        ~r2 = Routine.run({
            loop{
                ~seqRoutInternalFiltered.lace(state[1].sequencer.numberOfSteps).rotate(~routineStep).do({arg i, n;
                    if((i.isNumber).and(isModeStepSequencer).and(currentPageIndex == 1), {midiOut.noteOn(0, i, 27)});
                    state[1].sequencer.selectedStepDuration.wait;
                });
            };
        });

        ~r3 = Routine.run({
            loop{
                var note = ~seqRoutInternalFiltered.lace(state[1].sequencer.numberOfSteps).rotate(~routineStep).wrapAt(-1);

                if((note.isNumber).and(isModeStepSequencer).and(~modebtns.includes(note) == false).and(currentPageIndex == 1),
                {midiOut.noteOn(0, note, 60)});

                ~seqRoutInternalFiltered.lace(state[1].sequencer.numberOfSteps).rotate(~routineStep).do({arg i, n;

                    state[1].sequencer.selectedStepDuration.wait;
                    if((i.isNumber).and(isModeStepSequencer).and(currentPageIndex == 1), {midiOut.noteOn(0, i, 60)});
                });
            };
        });

        ~r4 = Routine.run({
            loop{
                ~seqCursorFiltered.lace(state[1].sequencer.numberOfSteps).rotate(~routineStep).do({arg i, n;
                    if((i.isNumber).and(isModeStepSequencer).and(currentPageIndex == 1), {midiOut.noteOn(0, i, 58)});
                    state[1].sequencer.selectedStepDuration.wait;
                });
            };
        });

        ~r5 = Routine.run({
            loop{
                var note = ~seqCursorFiltered.lace(state[1].sequencer.numberOfSteps).rotate(~routineStep).wrapAt(-1);

                if((note.isNumber).and(isModeStepSequencer).and(~modebtns.includes(note) == false).and(currentPageIndex == 1),
                {midiOut.noteOn(0, note, 16)});

                ~seqCursorFiltered.lace(state[1].sequencer.numberOfSteps).rotate(~routineStep).do({arg i, n;
                    state[1].sequencer.selectedStepDuration.wait;
                    if((i.isNumber).and(isModeStepSequencer).and(currentPageIndex == 1), {midiOut.noteOn(0, i, 16)});
                });
            };
        });

        MIDIFunc.noteOn({arg vel, note, ch, id;
            if (currentPageIndex == 1) {
                fork {
                    if (isCurrentLayoutDrumRack.not) {
                        if (((this.getNNForRightFunctionButtonAtIndex(0) == note).and(isModeStepSequencer)).or(
                            (this.getNNForRightFunctionButtonAtIndex(1) == note).and(isModeStepSequencerEdit))) {
                                this.redrawDrumRackGrid();
                        };

                        if ((isModeStepSequencer).and(~modebtns.includes(note) == false).and(note < state[1].sequencer.totalNumberOfSteps)) {
                            var flash;

                            flash = Routine.run({
                                loop{
                                    if ((currentPageIndex == 1).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencer)) {
                                        midiOut.noteOn(0, note, 60);
                                    };

                                    0.1.wait;

                                    if ((currentPageIndex == 1).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencer)) {
                                        midiOut.noteOn(0, note, 16);
                                    };

                                    0.1.wait;
                                };
                            });

                            Routine({
                                state[1].sequencer.selectedStepDuration.wait;

                                Routine({
                                    switch(~sequence[note],
                                    0, {
                                        ~sequence[note] = 1;
                                        ~seqRoutInternal[note] = note;
                                        if ((currentPageIndex == 1).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencer)) {
                                            midiOut.noteOn(0, note, 60);
                                        };
                                    },
                                    1, {
                                        ~sequence[note] = 0;
                                        ~seqRoutInternal[note] = nil;
                                        if ((currentPageIndex == 1).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencer)) {
                                            midiOut.noteOn(0, note, 16);
                                        };
                                    });

                                    ~routineStep = ~routineStep - ~routineStepExchange;

                                    ~r1.reset; 
                                    ~r2.reset;
                                    ~r3.reset; 
                                    ~r4.reset; 
                                    ~r5.reset;
                                    flash.stop;

                                    ~seqLedFeedback.value;
                                }).play(quant: state[1].sequencer.selectedStepDuration);
                            }).play;
                        };
                    };

                    Server.default.wait;

                    if (((note == 100).and(isCurrentLayoutDrumRack)).or((note == 8).and(isCurrentLayoutDrumRack.not))) {
                        midiOut.control(0, 0, 1); isCurrentLayoutDrumRack = false;
                        midiOut.noteOn(0, 8, 63); isModeStepSequencer = true;
                        midiOut.noteOn(0, 24, 30); isModeStepSequencerEdit = false;

                        state[1].sequencer.totalNumberOfSteps.do({arg n;
                            if(~modebtns.includes(n) == false, {
                                midiOut.noteOn(0, n, 16)});
                            });

                            if((state[1].sequencer.totalNumberOfSteps != 120).and(state[1].sequencer.totalNumberOfSteps != 0), {
                                (state[1].sequencer.totalNumberOfSteps..120).do({arg n;
                                    if(~modebtns.includes(n) == false, {
                                        midiOut.noteOn(0, n, 0);
                                    });
                                });
                            });

                            ~sequence.lace(state[1].sequencer.totalNumberOfSteps).do({arg i, n; if(i == 1, {midiOut.noteOn(0, n, 60)})});

                            ~seqLedFeedback.value;
                        };

                    };

                    Routine.run({
                        if (isCurrentLayoutDrumRack.not) {
                            if ((~modebtns.includes(note) == false).and(isModeStepSequencerEdit)) {
                                state[1].sequencer.totalNumberOfSteps = note + 1;

                                state[1].sequencer.totalNumberOfSteps.do({arg n;
                                    if(~modebtns.includes(n) == false, {
                                        midiOut.noteOn(0, n, 16);
                                    });
                                });

                                if((state[1].sequencer.totalNumberOfSteps != 120).and(state[1].sequencer.totalNumberOfSteps != 0), {
                                    (state[1].sequencer.totalNumberOfSteps..120).do({arg n;
                                        if(~modebtns.includes(n) == false, {
                                            midiOut.noteOn(0, n, 0);
                                        });
                                    });
                                });

                                state[1].sequencer.numberOfSteps = (((state[1].sequencer.totalNumberOfSteps.roundUp(8) / 2) - 4) + ((state[1].sequencer.totalNumberOfSteps - 1)%8 + 1)).asInteger;

                                ~r1.reset; ~r2.reset; ~r3.reset; ~r4.reset; ~r5.reset; ~stepCounter.reset;

                                ("Steps number changed to: " + state[1].sequencer.numberOfSteps.asString).postln;
                            };

                            Server.default.wait;

                            if ((note == 24).or(note == 101)) {
                                midiOut.control(0, 0, 1);
                                isCurrentLayoutDrumRack = false;
                                isModeStepSequencerEdit = true;
                                midiOut.noteOn(0, 24, 63);
                                isModeStepSequencer = false;
                                midiOut.noteOn(0, 8, 30);
                                state[1].sequencer.totalNumberOfSteps.do({arg n;
                                    if(~modebtns.includes(n) == false, {
                                        midiOut.noteOn(0, n, 16);
                                    });
                                });

                                if((state[1].sequencer.totalNumberOfSteps != 120).and(state[1].sequencer.totalNumberOfSteps != 0), {
                                    (state[1].sequencer.totalNumberOfSteps..120).do({arg n;
                                        if(~modebtns.includes(n) == false, {
                                            midiOut.noteOn(0, n, 0);
                                        });
                                    });
                                });
                            };
                        }; 
                    });
                };

                // Durations
                if((isCurrentLayoutDrumRack).and((102..106).includes(note))) {
                    midiOut.noteOn(0, note, 63);
                    (102..106).do({arg i, n;
                        if (note == i) {
                            state[1].sequencer.selectedStepDuration = state[1].sequencer.availableStepDurations[n]
                        } {
                            midiOut.noteOn(0, i, 29);
                        };
                    });
                };

                if(((isModeStepSequencer).or(isModeStepSequencerEdit)).and(isCurrentLayoutDrumRack.not).and([40, 56, 72, 88, 104].includes(note)), {
                    midiOut.noteOn(0, note, 63);
                    [40, 56, 72, 88, 104].do({arg i, n;
                        if (note == i) {
                            state[1].sequencer.selectedStepDuration = state[1].sequencer.availableStepDurations[n];
                        } {
                            midiOut.noteOn(0, i, 29);
                        };
                    });
                });
            }, srcID: deviceID);
	}

	initLoggerTextFields {
		AppClock.sched(0.0, {
			loggerTextFields.do({arg tf, n;
				tf.enabled = false;
				tf.string = "Sampler" + (n+1);
			});
		});
	}

    free {
        midiFuncs.do({arg midiFunc;
            midiFunc.free();
        });
    }
}

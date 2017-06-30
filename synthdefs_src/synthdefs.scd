(
SynthDef.synthDefDir = "~/Desktop/Iannis/synthdefs".standardizePath;

s.boot;
//
// Granulator
//

~metadata = (
  type: \synth,
  name: "Granulator",
  id: "by.alestsurko.granulator",
  author: "Ales Tsurko",
  site: "http://alestsurko.by",
  description: "Granular sampler.",
  ui: (
    // theme: (
      // // theme settings
    // ),
    pages: [
      // каждая страница -- отдельный таб
      (
        name: "OSC 1",
        groups: [
          // Bufnum group
          (
            // если параметра name нет, то группа не будет
            // выделяться как группа
            // параметр name может быть пустой строкой "",
            // тогда группа будет визуально группой, но
            // без имени
            parameters: [(
              key: \bufnum,
              ui: (
                type: \recorder
              )
            )]
          ),

          // (
            // name: "Elements test",
            // parameters: [
              // (
                // tabs: [
                  // (
                    // name: "First",
                    // parameters: [
                      // (
                        // key: \undefined,
                        // ui: (
                          // type: \button,
                          // align: \right,
                          // states: [
                            // (
                              // name: "First",
                              // value: {"first value generator".postln;1}
                            // ),
                            // (
                              // name: "Second",
                              // value: {"second value generator".postln;2}
                            // ),
                            // (
                              // name: "Third",
                              // value: {"third value generator".postln;3}
                            // )
                          // ]
                        // )
                      // ),
// 
                      // (
                        // key: \undefined_1,
                        // name: "Is true?",
                        // ui: (
                          // type: \check
                        // )
                      // ),
// 
                      // (
                        // key: \undefined_2,
                        // name: "Parameter 1",
                        // spec: ControlSpec(0.05, 3, \exp, 0, 0.1, "s"),
                        // ui: (
                          // type: \number
                        // )
                      // ),
// 
                      // (
                        // key: \undefined_3,
                        // name: "Parameter 2",
                        // spec: ControlSpec(0, 255, \lin, 1, 4, "n"),
                        // ui: (
                          // type: \number
                        // )
                      // ),
// 
                      // (
                        // key: \undefined_4,
                        // name: "Parameter 3",
                        // spec: ControlSpec(0.05, 3, \exp, 0, 0.1, "s"),
                        // ui: (
                          // type: \vrslider
                        // )
                      // ),
// 
                      // (
                        // key: \undefined_5,
                        // name: "Parameter 4",
                        // spec: ControlSpec(1, 1000, \exp, 1, 10, "ms"),
                        // ui: (
                          // type: \hrslider
                        // )
                      // ),
// 
                      // (
                        // key: \undefined_6,
                        // name: "Parameter 5",
                        // spec: [
                          // ControlSpec(0.05, 3, \exp, 0, 0.1, "s"),
                          // ControlSpec(100, 8000, \exp, 0.1, 400, "Hz")
                        // ],
                        // ui: (
                          // type: \xy
                        // )
                      // )
                    // ]
                  // ),
// 
                  // (
                    // name: "Second",
                    // parameters: [
                      // (
                        // key: \undefined_7,
                        // name: "Parameter",
                        // spec: \unipolar,
                        // ui: (
                          // type: \knob
                        // )
                      // ),
// 
                      // (
                        // key: \undefined_8,
                        // name: "Parameter",
                        // spec: \unipolar,
                        // ui: (
                          // type: \vslider
                        // )
                      // )
                    // ]
                  // ),
// 
                  // (
                    // name: "Third",
                    // parameters: [
                      // (
                        // isRow: 0,
                        // align: \center,
                        // parameters: [
                          // (
                            // key: \undefined_9,
                            // name: "Parameter 1",
                            // spec: \unipolar,
                            // ui: (
                              // type: \knob
                            // )
                          // ),
// 
                          // (
                            // key: \undefined_10,
                            // name: "Parameter 2",
                            // spec: \unipolar,
                            // ui: (
                              // type: \knob
                            // )
                          // ),
// 
// 
                          // (
                            // key: \undefined_11,
                            // name: "Parameter 3",
                            // spec: \unipolar,
                            // ui: (
                              // type: \knob
                            // )
                          // ),
                        // ]
                      // ),
// 
                      // (
                        // key: \undefined_12,
                        // name: "Parameter 4",
                        // ui: (
                          // type: \custom,
                          // align: \center,
                          // init: {arg key, name, uiObj;
                            // var view = Knob();
                            // view.fixedHeight = 40;
                            // // should return the view, that have to be
                            // // a subclass of View
                            // view;
                          // },
                          // action: {arg view, key, node, uiObj;
                            // var value = view.value;
// 
                            // node.set(key, value);
// 
                            // // should return the value, that you want to
                            // // store in preset
                            // value;
                          // },
                          // binder: {arg view, value, uiObj;
                            // // this function loading a value, stored in a preset
                            // view.value = value;
                          // }
                        // )
                      // )
                    // ]
                  // )
                // ]
              // )
            // ]
          // ),

          // Grain Parameters Group
          (
            name: "Grain Parameters",
            parameters: [(
              key: \gran_env_bufnum,
              name: "Grain Envelope",
              ui: (
                type: \popup,
                align: \left,
                items: [
                  (
                    name: "Rectangle",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      signal = Signal.rectWindow(bufSize);
                      buffer = Buffer.loadCollection(Server.default, signal);

                      // return
                      buffer.bufnum;
                    }
                  ),
                  (
                    name: "Hanning",
                    value: -1
                  ),
                  (
                    name: "Hamming",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      signal = Signal.hammingWindow(bufSize);
                      buffer = Buffer.loadCollection(Server.default, signal);

                      // return
                      buffer.bufnum;
                    }
                  ),
                  (
                    name: "Welch",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      signal = Signal.welchWindow(bufSize);
                      buffer = Buffer.loadCollection(Server.default, signal);

                      // return
                      buffer.bufnum;
                    }
                  )
                ]
              )
            ),
            (
              // parameters row
              isRow: 0,
              align: \center,

              parameters: [(
                key: \gran_dur,
                name: "Duration",
                spec: ControlSpec(0.05, 3, \exp, 0, 0.1, "s"),
                ui: (
                  type: \knob
                )
              ),
              (
                key: \pos,
                name: "Position",
                spec: \unipolar,
                ui: (
                  type: \knob
                )
              ),
              (
                key: \density,
                name: "Density",
                spec: ControlSpec(1, 16, \lin, 1, 1),
                ui: (
                  type: \knob
                )
              ),
              (
                key: \t1rand,
                name: "Randomness",
                spec: \unipolar,
                ui: (
                  type: \knob
                )
              )]
            )
          ]),

          // Envelope Group
          (
            name: "Envelope",
            parameters: [(
              key: \env,
              name: "Envelope",
              ui: (
                type: \env_adsr
              )
            )]
          ),

          // Output group
          (
            name: "Output",
            parameters: [(
              isRow: 0,
              align: \center,

              parameters: [(
                key: \gain,
                name: "Gain",
                spec: ControlSpec(0.ampdb, 24, \db, units: "dB"),
                ui: (
                  type: \hslider
                )
              ),
              (
                key: \pan,
                name: "Pan",
                spec: \pan,
                ui: (
                  type: \hslider
                )
              )]
            )]
          )
        ]
      )
    ]
  ),
  presets: [
    (
      name: "Default",
      isFactory: true,
      values: (
        // bufnum: "here/should/be/a/path/to/a/file",
        env: [0.1, 0.3, 0.5, 1, -1, 0, -2],
        gran_env_bufnum: 1, // popup menu item index
        gran_dur: 1,
        pos: 0,
        density: 4,
        t1rand: 0.1,
        gain: 0.0,
        pan: 0
      )
    ),
    (
      name: "Another Preset",
      isFactory: true,
      values: (
        env: [2, 0.5, 0.3, 4, -1, 0, -2],
        gran_env_bufnum: 0, // popup menu item index
        gran_dur: 2,
        pos: 0.1,
        density: 1,
        t1rand: 0,
        gain: 0.0,
        pan: 0.1
      )
    ),
    (
      name: "Preset 3",
      isFactory: true,
      values:
      (
        env: [0.05, 1, 1, 0.5, 0, 0, 2],
        gran_env_bufnum: 2, // popup menu item index
        gran_dur: 0.1,
        pos: 0.5,
        density: 8,
        t1rand: 0.7,
        gain: 0.0,
        pan: {rrand(-1.0, 1.0)}
      )
    )
  ]
);
)

SynthDef("granulator", {arg bufnum, gran_env_bufnum = -1, 
  gran_dur = 1, pos = 0.0, density = 4, t1rand = 0.1,
  // trig, t2gpan = 0, t2gamp = 1, t2gatt, t2gdec,
  env = #[0.005, 0.01, 1, 0.1, 0, 0, 0], gain = 0, pan = 0.0, velocity, freq, gate = 1, out = 0;

  var veloamp = velocity/127;
  var note = (freq.cpsmidi-60).midiratio;
  var envelope = EnvGen.kr(Env.adsr(env[0], env[1], env[2], env[3], 1, env[4..6]), gate, doneAction: 2);
  var output = Warp1.ar(1, bufnum, pos, note*BufRateScale.kr(bufnum), gran_dur, gran_env_bufnum, density, t1rand, 4);
	// Note: Unlike PlayBuf, the TGrains3 rate is multiplied by BufRate, so you needn't do that yourself.
	// var tgrains = TGrains3.ar(2, trig, bufnum, note, BufDur.kr(bufnum)*pos, grain_dur, t2gpan, t2gatt, t2gdec, 1, 4);
  // var output = Select.ar(type, [warp, tgrains]); // здесь выбор в соответствии с типом синтеза
  output = output * envelope * veloamp;

  Out.ar(out, Pan2.ar(output, pan, gain.dbamp));
}, [\ir, nil,
	0, 0, 0, 0, 
	0, 0.01, 0.01, 0, 0, nil, \ir],
	metadata: ~metadata
).writeDefFile(SynthDef.synthDefDir);


//
// FX
//

(
//
// EQ
//
var metadata = ();


// main info
metadata[\type] = \effect;
metadata[\name] = "Parameq";
metadata[\id] = "by.alestsurko.iannis.parameq";
metadata[\author] = "Ales Tsurko";
metadata[\site] = "http://alestsurko.by";
metadata[\description] = "4-band parametric EQ with LP, HP, Lo-Shelf and Hi-Shelf filters.";


// UI
metadata[\ui] = (pages: []);

// pages
metadata[\ui][\pages] = [(
  name: "EQ",
  groups: nil!3
)];

// groups

// Peak columns
~peakColumns = [];

4.do({arg n;
  var bypassKey = ("peak"++(n+1)++"Bypass").asSymbol;
  var onStateName = (n+1) + "- On";
  var offStateName = (n+1) + "- Off";
  var freqKey = ("peak"++(n+1)++"Freq").asSymbol;
  var freqName = "Peak"+(n+1);
  var freqSpec = ControlSpec(20, 22000, \exp, 0, 120*(2**n), "Hz");
  var qKey = ("peak"++(n+1)++"Q").asSymbol;
  var qSpec = ControlSpec(0.01, 10);
  var gainKey = ("peak"++(n+1)++"Gain").asSymbol;
  var gainSpec = ControlSpec(0.ampdb, 24, \db, 0, 0, "dB");

  ~peakColumns = ~peakColumns.add(
    (
      isColumn: 1,
      align: \center,
      parameters: [
        // bypass button
        (
          key: bypassKey,
          ui: (
            type: \button,
            width: 50,
            states: [
              (
                name: onStateName,
                value: 0
              ),
              (
                name: offStateName,
                value: 1
              )
            ]
          )
        ),

        // frequency knob
        (
          key: freqKey,
          name: freqName,
          spec: freqSpec,
          ui: (
            type: \knob
          )
        ),

        // Q knob
        (
          key: qKey,
          name: "Q",
          spec: qSpec,
          ui: (
            type: \knob
          )
        ),

        // gain knob
        (
          key: gainKey,
          name: "Gain",
          spec: gainSpec,
          ui: (
            type: \knob
          )
        )
      ]
    )
  );
});

// Peaks
metadata[\ui][\pages][0][\groups][0] = (
  parameters: [
    (
      isRow: 1,
      align: \center,
      parameters: ~peakColumns
    )
  ]
);

// LS, HS group
metadata[\ui][\pages][0][\groups][1] = (
  parameters: [
    // Bypass row
    (
      isRow: 1,
      align: \center,
      parameters: [
        
        (
          key: \loShelfBypass,
          ui: (
            type: \button,
            width: 60,
            states: [
              (
                name: "LS - On",
                value: 0
              ),
              (
                name: "LS - Off",
                value: 1
              )
            ]
          )
        ),

        (
          key: \hiShelfBypass,
          ui: (
            type: \button,
            width: 60,
            states: [
              (
                name: "HS - On",
                value: 0
              ),
              (
                name: "HS - Off",
                value: 1
              )
            ]
          )
        )
      ]
    ),

    // Freq row
    (
      isRow: 1,
      align: \center,
      parameters: [
        (
          key: \loShelfFreq,
          name: "LS Freq",
          spec: ControlSpec(20, 22000, \exp, 0, 150, "Hz"),
          ui: (
            type: \knob
          )
        ),

        (
          key: \hiShelfFreq,
          name: "HS Freq",
          spec: ControlSpec(20, 22000, \exp, 0, 7900, "Hz"),
          ui: (
            type: \knob
          )
        )
      ]
    ),

    // Q row
    (
      isRow: 1,
      align: \center,
      parameters: [
        (
          key: \loShelfQ,
          name: "Q",
          spec: ControlSpec(0.01, 10),
          ui: (
            type: \knob
          )
        ),

        (
          key: \hiShelfQ,
          name: "Q",
          spec: ControlSpec(0.01, 10),
          ui: (
            type: \knob
          )
        )
      ]
    ),

    // Gain row
    (
      isRow: 1,
      align: \center,
      parameters: [
        (
          key: \loShelfGain,
          name: "Gain",
          spec: ControlSpec(0.ampdb, 24, \db, 0, 0, "dB"),
          ui: (
            type: \knob
          )
        ),

        (
          key: \hiShelfGain,
          name: "Gain",
          spec: ControlSpec(0.ampdb, 24, \db, 0, 0, "dB"),
          ui: (
            type: \knob
          )
        )
      ]
    )
  ]
);

// LP, HP group
metadata[\ui][\pages][0][\groups][2] = (
  parameters: [
    // Bypass row
    (
      isRow: 1,
      align: \center,
      parameters: [
        
        (
          key: \hpBypass,
          ui: (
            type: \button,
            width: 60,
            states: [
              (
                name: "HP - On",
                value: 0
              ),
              (
                name: "HP - Off",
                value: 1
              )
            ]
          )
        ),

        (
          key: \lpBypass,
          ui: (
            type: \button,
            width: 60,
            states: [
              (
                name: "LP - On",
                value: 0
              ),
              (
                name: "LP - Off",
                value: 1
              )
            ]
          )
        )
      ]
    ),

    // Freq row
    (
      isRow: 1,
      align: \center,
      parameters: [
        (
          key: \hpFreq,
          name: "HP Freq",
          spec: ControlSpec(20, 22000, \exp, 0, 20, "Hz"),
          ui: (
            type: \knob
          )
        ),

        (
          key: \lpFreq,
          name: "LP Freq",
          spec: ControlSpec(20, 22000, \exp, 0, 22000, "Hz"),
          ui: (
            type: \knob
          )
        )
      ]
    ),

    // Q row
    (
      isRow: 1,
      align: \center,
      parameters: [
        (
          key: \hpQ,
          name: "Q",
          spec: ControlSpec(0.01, 10),
          ui: (
            type: \knob
          )
        ),

        (
          key: \lpQ,
          name: "Q",
          spec: ControlSpec(0.01, 10),
          ui: (
            type: \knob
          )
        )
      ]
    )
  ]
);

// presets
metadata[\presets] = [];
metadata[\presets] = metadata[\presets].add(
  (
    name: "Default",
    isFactory: true,
    values: (
      /*LP*/
      lpFreq: 22000,
      lpQ: 2,
      lpBypass: 0,
      /*HP*/
      hpFreq: 20,
      hpQ: 2,
      hpBypass: 0,
      /*LoShelf*/
      loShelfFreq: 120,
      loShelfQ: 2,
      loShelfGain: 0,
      loShelfBypass: 0,
      /*HiShelf*/
      hiShelfFreq: 12700,
      hiShelfQ: 2,
      hiShelfGain: 0,
      hiShelfBypass: 0,
      /*Peak 1*/
      peak1Freq: 250,
      peak1Q: 2,
      peak1Gain: 0,
      peak1Bypass: 0,
      /*Peak 2*/
      peak2Freq: 1200,
      peak2Q: 2,
      peak2Gain: 0,
      peak2Bypass: 0,
      /*Peak 3*/
      peak3Freq: 3500,
      peak3Q: 2,
      peak3Gain: 0,
      peak3Bypass: 0,
      /*Peak 4*/
      peak4Freq: 8000,
      peak4Q: 2,
      peak4Gain: 0,
      peak4Bypass: 0,
    )
  )
);

SynthDef("by-alestsurko-iannis-parameq", {arg 
  in = 0,
  lpFreq = 20000, lpQ = 2, lpBypass = 1,                                  // LP
  hpFreq = 20, hpQ = 2, hpBypass = 1,                                     // HP
  loShelfFreq = 120, loShelfQ = 2, loShelfGain = 0, loShelfBypass = 0,    // LoShelf
  hiShelfFreq = 12700, hiShelfQ = 2, hiShelfGain = 0, hiShelfBypass = 0,  // HiShelf
  peak1Freq = 200, peak1Q = 2, peak1Gain = 0, peak1Bypass = 0,            // Peak 1
  peak2Freq = 800, peak2Q = 2, peak2Gain = 0, peak2Bypass = 0,            // Peak 2
  peak3Freq = 3200, peak3Q = 2, peak3Gain = 0, peak3Bypass = 0,           // Peak 3
  peak4Freq = 4500, peak4Q = 2, peak4Gain = 0, peak4Bypass = 0;           // Peak 4
  
  var source, 
  lp,
  hp,
  loShelf,
  hiShelf,
  peak1,
  peak2,
  peak3,
  peak4,
  out;

  source = In.ar(in, 2);
 
  lp = BLowPass4.ar(source, lpFreq, lpQ.reciprocal);
  lp = (source * lpBypass) + (lp * (lpBypass < 0.5));

  hp = BHiPass4.ar(lp, hpFreq, hpQ.reciprocal);
  hp = (lp * hpBypass) + (hp * (hpBypass < 0.5));
  
  loShelf = BLowShelf.ar(hp, loShelfFreq, loShelfQ.reciprocal, loShelfGain);
  loShelf = (hp * loShelfBypass) + (loShelf * (loShelfBypass < 0.5));

  hiShelf = BHiShelf.ar(loShelf, hiShelfFreq, hiShelfQ.reciprocal, hiShelfGain);
  hiShelf = (loShelf * hiShelfBypass) + (hiShelf * (hiShelfBypass < 0.5));

  peak1 = BPeakEQ.ar(hiShelf, peak1Freq, peak1Q.reciprocal, peak1Gain);
  peak1 = (hiShelf * peak1Bypass) + (peak1 * (peak1Bypass < 0.5));

  peak2 = BPeakEQ.ar(peak1, peak2Freq, peak2Q.reciprocal, peak2Gain);
  peak2 = (peak1 * peak2Bypass) + (peak2 * (peak2Bypass < 0.5));

  peak3 = BPeakEQ.ar(peak2, peak3Freq, peak3Q.reciprocal, peak3Gain);
  peak3 = (peak2 * peak3Bypass) + (peak3 * (peak3Bypass < 0.5));

  peak4 = BPeakEQ.ar(peak3, peak4Freq, peak4Q.reciprocal, peak4Gain);
  peak4 = (peak3 * peak4Bypass) + (peak4 * (peak4Bypass < 0.5));

  out = peak4;

  ReplaceOut.ar(0, out);
}, metadata: metadata).writeDefFile(SynthDef.synthDefDir);
)

IannisAceWrapper : WebView {
  var text, condition,
  <>onEvaluate,
  <>onEvaluateSelection,
  <>onHardStop;
  /* 
  Ctrl-R to evaluate the entire document or
  Shift-Enter to evaluate a line or selection.
  Ctrl-` - switching between Vim/Normal mode.
  Ctrl-Alt-H - show keyboard shortcuts.
  */
  *new {
    ^super.new.init()
  }

  init {
    condition = Condition();

    this.url = "index.html".resolveRelative;

    this.onJavaScriptMsg = {arg view, msg;
      this.parseConsoleMessage(msg);
    };
  }

  parseConsoleMessage {arg msg;
    case 
    // evaluate all
    {msg.contains("<-!code_evaluation_triggered!->")} {
      var code = msg.replace("<-!code_evaluation_triggered!->");
      this.onEvaluate.value(code);
      AppClock.sched(0, {condition.unhang()});
    }
    // evaluate a selection or line
    {msg.contains("<-!selection_evaluation_triggered!->")} {
      var code = msg.replace("<-!selection_evaluation_triggered!->");
      this.onEvaluateSelection.value(code);
      AppClock.sched(0, {condition.unhang()});
    }
    //hard stop
    {msg.contains("<-!hard_stop_triggered!->")} {
      this.onHardStop.value();
      AppClock.sched(0, {condition.unhang()});
    }
    // get value
    {msg.contains("<-!get_value_triggered!->")} {
      var code = msg.replace("<-!get_value_triggered!->");
      text = code;
      AppClock.sched(0, {condition.unhang()});
    }
    // else
    {true} {
      ("Ace log:"+msg).postln;
    };
  }

  setValue {arg newValue;
    var convertedString;

    newValue!?{
      convertedString = newValue.replace("\n", "\\n").asSymbol.asCompileString;
    }??{
      convertedString = "";
    };

    this.evaluateJavaScript("editor.setValue("++convertedString++")");
  }

  // update the text, then calls the passed
  // callback (if any) with the text as argument
  getValue {arg callback;
    AppClock.play(
      Routine({
        this.evaluateJavaScript("console.log(\"<-!get_value_triggered!->\"+editor.getValue())");
        condition.hang();
        callback!?{callback.value(text)};
      });
    );
  }

  // evaluates entire document, then calls the callback (if any)
  evaluate {arg callback;
    AppClock.play(
      Routine({
        this.evaluateJavaScript("console.log(\"<-!code_evaluation_triggered!->\"+editor.getValue())");
        condition.hang();
        callback!?{callback.value()};
      });
    );
  }

  // evaluates selection or current line, then calls the callback (if any)
  evaluateSelection {arg callback;
    AppClock.play(
      Routine({
        var jsString = "var text = editor.session.getTextRange(editor.getSelectionRange());
        if (text.length > 0) {
          console.log(\"<-!selection_evaluation_triggered!->\"+text);
        } else {
          var currentLineNumber = editor.selection.getCursor().row;
          var currentLineText = editor.session.getLine(currentLineNumber);
          // return current line
          console.log(\"<-!selection_evaluation_triggered!->\"+currentLineText);
        }";

        this.evaluateJavaScript(jsString);

        condition.hang();
        callback!?{callback.value()};
      });
    );
  }

  // hard stop (Cmd-.)
  hardStop {arg callback;
    AppClock.play(
      Routine({
        this.evaluateJavaScript("console.log(\"<-!hard_stop_triggered!->\")");
        condition.hang();
        callback!?{callback.value()};
      })
    );
  }
}

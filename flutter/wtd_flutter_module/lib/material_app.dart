import 'package:flutter/material.dart';
import 'package:flutter_typeahead/flutter_typeahead.dart';

import 'data.dart';

class MyMaterialApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Datamuse Autocomplete Demo',
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('Autocomplete'),
        ),
        body: NavigationExample());
  }
}

class NavigationExample extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.all(32.0),
      child: Column(
        children: <Widget>[
          SizedBox(
            height: 10.0,
          ),
          TypeAheadField(
            textFieldConfiguration: TextFieldConfiguration(
              autofocus: true,
              style: DefaultTextStyle.of(context)
                  .style
                  .copyWith(fontStyle: FontStyle.italic),
              decoration: InputDecoration(
                  border: OutlineInputBorder(),
                  hintText: 'Type the title of a story (3 characters min.)'),
            ),
            suggestionsCallback: (pattern) async {
              return await BackendService.getSuggestions(pattern);
            },
            itemBuilder: (context, Story suggestion) {
              return ListTile(
                title: Text(suggestion.code ?? 'none'),
                subtitle: Text('${suggestion.score ?? 0}'),
              );
            },
            onSuggestionSelected: (Story suggestion) {
              // your implementation here
            },
          ),
        ],
      ),
    );
  }
}
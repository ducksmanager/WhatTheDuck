import 'dart:async';
import 'dart:convert' as convert;

import 'package:http/http.dart' as http;

class BackendService {
  static Future<List<Map<String, String>>> getSuggestions(String query) async {
    if (query.isEmpty && query.length < 3) {
      print('Query needs to be at least 3 chars');
      return Future.value([]);
    }
    var url = Uri.https('api.datamuse.com', '/sug', {'s': query});

    var response = await http.get(url);
    List<Suggestion> suggestions = [];
    if (response.statusCode == 200) {
      Iterable json = convert.jsonDecode(response.body);
      suggestions =
      List<Suggestion>.from(json.map((model) => Suggestion.fromJson(model)));

      print('Number of suggestion: ${suggestions.length}.');
    } else {
      print('Request failed with status: ${response.statusCode}.');
    }

    return Future.value(suggestions
        .map((e) => {'name': e.word, 'score': e.score.toString()})
        .toList());
  }
}

class Suggestion {
  final int score;
  final String word;

  Suggestion({
    required this.score,
    required this.word,
  });

  factory Suggestion.fromJson(Map<String, dynamic> json) {
    return Suggestion(
      word: json['word'],
      score: json['score'],
    );
  }
}
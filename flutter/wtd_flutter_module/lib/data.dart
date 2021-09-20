import 'dart:async';

import 'package:dio/dio.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logger/logger.dart';

import 'api/dm_server_api.dart';

class BackendService {
  final logger = Logger();
  static Future<List<Story>> getSuggestions(String query) async {
    if (query.isEmpty || query.length < 3) {
      print('Query needs to be at least 3 chars');
      return Future.value([]);
    }

    final client = RestClient(Dio(BaseOptions(contentType: "application/json")));
    return client
        .listStoriesFromKeywords({ 'keywords': query })
        .then((value) => value.list)
        .catchError((e) {
      print("Exception occurred: $e");
      return List<Story>.empty();
    });
  }
}

@JsonSerializable()
class Story {
  final int? score;
  final String? code;

  Story({
    required this.score,
    required this.code,
  });

  factory Story.fromJson(dynamic json) {
    return Story(
      code: json['word'],
      score: json['score'],
    );
  }
}

@JsonSerializable()
class StoryList {
  final List<Story> list;

  StoryList({
    required this.list,
  });

  factory StoryList.fromJson(Map<String, dynamic> json) {
    return StoryList(
      list: json['results']
    );
  }
}
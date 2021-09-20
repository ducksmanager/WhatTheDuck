import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

import '../data.dart';

part 'dm_server_api.g.dart';

@RestApi(baseUrl: "https://api.ducksmanager.net")
abstract class RestClient {
  factory RestClient(Dio dio, {String baseUrl}) = _RestClient;

  @POST("/coa/stories/search")
  Future<StoryList> listStoriesFromKeywords(@Body() Map<String, dynamic> map);
}
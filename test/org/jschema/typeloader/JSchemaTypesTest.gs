package org.jschema.typeloader

uses java.util.*
uses java.lang.*

uses org.jschema.test.*

uses org.jschema.examples.fullexample.Example
uses org.jschema.examples.fullexample.Example.SomeType
uses org.jschema.examples.fullexample.Example.SomeType.TypeInArray
uses org.jschema.examples.fullexample.Example.SomeType.EnumEx
uses org.jschema.examples.fullexample.Example.SomeType.NestedType
uses org.jschema.examples.fullexample.Example.SomeType.NestedType.NestedTypeInArray

uses org.jschema.examples.twitter.status.StatusResponse
uses org.jschema.examples.twitter.status.StatusResponse.User

uses org.jschema.examples.google.geocode.GeocodeResponse
uses org.jschema.examples.google.geocode.GeocodeResponse.Results
uses org.jschema.examples.google.geocode.GeocodeResponse.Results.Types
uses org.jschema.examples.google.geocode.GeocodeResponse.Results.Geometry
uses org.jschema.examples.google.geocode.GeocodeResponse.Results.Geometry.Location

uses org.jschema.examples.PeopleId
uses org.jschema.examples.PeopleId.IdToPeople
uses org.jschema.examples.PeopleId.IdToPeople.EyeColor
uses org.jschema.examples.NameAndAge

uses org.jschema.examples.people1.Peeps
uses org.jschema.examples.people1.Peeps.People

class JSchemaTypesTest extends GosonTest {

  function testOne() {
    /* { "name" : "string", "age" : "integer" } */
    /* { "name" : "Joe", "age" : "42" } */
    var person = new NameAndAge() {
      :Name = "Joe",
      :Age = 42
    }
    print(NameAndAge.parse(person.write()).write())

    /*
    { "people" : [ { "name" : "string",
                     "age" : "integer"} ] }
    */
    /*
    { "people" : [
      { "name" : "Joe", "age" : "42" }
      { "name" : "Paul", "age" : "28" }
      { "name" : "Mack", "age" : "55" } ] }
    */
    var people = new Peeps() {
      :People = {
        new People() { :Name = "Joe", :Age = 42 },
        new People() { :Name = "Paul", :Age = 28 },
        new People() { :Name = "Mack", :Age = 55 }
      }
    }
    print(Peeps.parse(people.write()).write())

    /*
    { "people" : [ { "name" : "string",
                     "age" : "integer",
                     "eye_color" : {"enum" : ["brown",
                                              "blue",
                                              "green"]}} ] }
    */
    /*
    { "people" : [
      { "name" : "Joe", "age" : "42", "eye_color" : "brown" },
      { "name" : "Paul", "age" : "28", "eye_color" : "brown" },
      { "name" : "Mack", "age" : "55", "eye_color" : "blue" } ] }
    */
    /*var people2 = new jschema.people2.Peeps() {
      :People = {
        new jschema.people2.People() { :Name = "Joe", :Age = 42, :EyeColor = jschema.people2.EyeColor.BROWN },
        new jschema.people2.People() { :Name = "Paul", :Age = 28, :EyeColor = jschema.people2.EyeColor.BROWN },
        new jschema.people2.People() { :Name = "Mack", :Age = 55, :EyeColor = jschema.people2.EyeColor.BLUE }
      }
    }
    print(jschema.people2.Peeps.parse(people2.write()).write())*/

    /*
    { "id_to_people" : {
        "map_of" : { "name" : "string",
                    "age" : "integer",
                    "eye_color" : {"enum" : ["brown",
                                             "blue",
                                             "green"]}
        }
      }
    }
    */
    /*
    { "id_to_people" : {
        "1" : { "name" : "Joe", "age" : "42", "eye_color" : "brown" },
        "2" : { "name" : "Paul", "age" : "28", "eye_color" : "brown" },
        "3" : { "name" : "Mack", "age" : "55", "eye_color" : "blue" }
      }
    }
    */
    var peopleMap = new PeopleId() {
      :IdToPeople = {
        "1" -> new IdToPeople() { :Name = "Joe", :Age = 42, :EyeColor = EyeColor.BROWN },
        "2" -> new IdToPeople() { :Name = "Paul", :Age = 28, :EyeColor = EyeColor.BROWN },
        "3" -> new IdToPeople() { :Name = "Mack", :Age = 55, :EyeColor = EyeColor.BLUE }
      }
    }
    print(PeopleId.parse(peopleMap.write()).write())
    print(peopleMap.prettyPrint())
  }

  function testTwo() {
    var example = new Example() {
      :SomeType = new SomeType() {
        :BigIntEx = 12312,
        :StringEx = "Example",
        :BooleanEx = true,
        :BigDecimalEx = 20.1231,
        :IntEx = 1,
        :DecimalEx = 1.2123,
        :TypeInArray = {
          new TypeInArray() {
            :Content = "Example Content"
          }
        },
        :MapEx = { "1232" -> "OneTwoThreeTwo", "111" -> "OneOneOne" },
        :EnumEx = EnumEx.JSON,
        :NestedType = new NestedType() {
          :NestedStringEx = "Nested Example",
          :NestedTypeInArray = {
            new NestedTypeInArray() {
              :Value = "Super Nested",
              :ADate = new java.util.Date()
            }
          },
          :BigIntArrayEx = { 312 },
          :StringArrayEx = { "Oh Nice", "This", "Is", "An", "Array" },
          :NestedBigIntEx = 12312,
          :NestedBigDecimalEx = 123.1239141
        }
      }
    }

    print(example.SomeType.NestedType.StringArrayEx.join(" "))
    print(Example.parse(example.write()).write())
    print("")

    var status = new StatusResponse() {
      :InReplyToStatusId = 1232,
      //TODO :User = new org.jschema.examples.twitter.status.StatusResponse.User() why won't this work?
      :User = new User() {
        :Name = "jpcamara",
        :CreatedAt = new java.util.Date(),
        :Url = "http://twitter.com/jpcamara",
        :Id = 12312312,
        :GeoEnabled = true
      }
    }
    print(status.write())
    print("")

    /*
    {
      "status": "string",
      "results": [ {
        "types": [{ "enum" : [ "street_address" ] }],
        "formatted_address": "string",
        "address_components": [{
          "long_name": "string",
          "short_name": "string",
          "types": [{ "enum" : [ "street_number" ] }    }
      } ]
    }
    */
    var geocode = new GeocodeResponse() {
      :Status = "Good",
      :Results = {
        new Results() {
          :Types = {
            Types.STREET_ADDRESS
          },
          :FormattedAddress = "123 Main St, Boulder CO",
          :Geometry = new Geometry() {
            :Location = new Location() {
              :Lat = -123.123123,
              :Lng = 12.12312
            }
          }
        }
      }
    }

    print(GeocodeResponse.parse(geocode.write()).write())
    print(GeocodeResponse.parse(new java.net.URL(
      "http://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=false"))
      .prettyPrint())
  }

}
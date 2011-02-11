package com.wideplay.crosstalk.data;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStore {
  static {
    // All managed data types.
    ObjectifyService.register(Document.class);
    ObjectifyService.register(Room.class);
  }

  @Inject
  private Objectify objectify;
//
//  public void save(Document document) {
//    objectify.put(document);
//  }
//
//  public Document fetch(long id) {
//    return objectify.query(Document.class).filter("id", id).get();
//  }
//
//  /**
//   * Retrieves all the wikis for the specified user.
//   */
//  public List<Room> listWikis(String owner) {
//    return objectify.query(Room.class).filter("owner", owner).list();
//  }
//
//  /**
//   * Lists documents by wiki id.
//   */
//  public List<Document> list(Long wikiId) {
//    return objectify.query(Document.class).filter("wiki.id", wikiId).list();
//  }
//
//  /**
//   * Creates a brand new wiki!
//   */
//  public void create(Room wiki) {
//    objectify.put(wiki);
//  }
}

package com.eliall.process;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.eliall.common.Database;
import com.eliall.common.EliObject;

@SuppressWarnings("rawtypes")
public class Board {
	public class Article {
		public List items(EliObject parameters) {
			SqlSession session = null;

			try { return (session = Database.session(false)).selectList("board-article-items", parameters); } finally { Database.release(session); }
		}

		public void list(EliObject parameters) {
			SqlSession session = null;
			List list = null;
			
			int count = 0, page = parameters.getInt("page", 1), size = Math.min(parameters.getInt("size", 15), 100), units = 10, pages = 0, groups = 0;

			parameters.put("start", size * (page - 1));
			parameters.put("end", Math.min(size, parameters.getInt("end", size)));

			try {
				session = Database.session(false);
				list = session.selectList("board-article-list", parameters);
				count = session.selectOne("board-article-count", parameters);

				pages = count / size; if (count % size > 0) pages++;
				groups = pages / units; if (pages % units > 0) groups++;

				parameters.put("units", units);
				parameters.put("pages", pages);
				parameters.put("groups", groups);
				parameters.put("group", page / units + 1);
				parameters.put("start", page / units * units + 1);
				parameters.put("end", Math.min(page / units * units + units, pages));

				parameters.put("page", page);
				parameters.put("size", size);
				parameters.put("count", count);
				parameters.put("result", list);
			} finally { Database.release(session); }
		}
		
		public void detail(EliObject parameters) {
			SqlSession session = null;
			Map result = null;
			
			try {
				session = Database.session(false);
				result = session.selectOne("board-article-detail", parameters);
				
				parameters.put("result", result);
			} finally { Database.release(session); }
		}

		public void write(EliObject parameters) throws Exception {
			create(parameters.set("table", "article").set("depth", parameters.getInt("depth")));
		}
		
		public void modify(EliObject parameters) throws Exception {
			modify(parameters.set("table", "article"));
		}
		
		public void remove(EliObject parameters) {
			remove(parameters);
		}
		
		public void action(EliObject parameters) throws Exception {
			SqlSession session = null;
			
			try {
				if ((session = Database.session(true)).insert("board-article-" + parameters.getString("act"), parameters) > 0) Database.commit(session);
			} catch (Exception e) { Database.rollback(session); throw e; } finally { Database.release(session); }
		}
	}
	
	public class Comment {
		public void list(EliObject parameters) {
			SqlSession session = null;
			List list = null;
			
			int count = 0, page = parameters.getInt("page", 1), size = Math.min(parameters.getInt("size", 30), 100);

			parameters.put("start", size * (page - 1));
			parameters.put("end", Math.min(size, parameters.getInt("end", size)));

			try {
				session = Database.session(false);
				count = session.selectOne("board-comment-count", parameters);
				list = session.selectList("board-comment-select", parameters);

				parameters.put("page", page);
				parameters.put("size", size);
				parameters.put("count", count);
				parameters.put("result", list);
			} finally { Database.release(session); }
		}
		
		public void write(EliObject parameters) throws Exception {
			create(parameters.set("table", "comment").set("depth", parameters.getInt("depth")));
		}
		
		public void modify(EliObject parameters) throws Exception {
			modify(parameters.set("table", "comment"));
		}
		
		public void remove(EliObject parameters) {
			remove(parameters);
		}
	}
	
	protected void create(EliObject parameters) throws Exception {
		SqlSession session = null;
		Map sequence = null;
		
		try {
			session = Database.session(true);
			sequence = session.selectOne("common-sequence");

			if (!parameters.getString("seq", "").equals("")) {
				parameters.put("parent", parameters.get("seq"));
				parameters.put("depth", parameters.getInt("depth") + 1);
			}

			if (parameters.getString("seq", "").equals("")) parameters.remove("seq");
			if (parameters.getString("bunch", "").equals("")) parameters.put("bunch", parameters.getString("seq", (String)sequence.get("seq_string")));

			parameters.put("seq", sequence.get("seq_string"));

			if (!parameters.getString("parent", "").equals("")) session.update("board-" + parameters.get("table") + "-replied", parameters);
			
			if (session.insert("board-" + parameters.get("table") + "-insert", parameters) > 0) Database.commit(session);
			else throw new Exception("Failed to create new affiliate data");
		} catch (Exception e) { Database.rollback(session); throw e; } finally { Database.release(session); }
	}
	
	protected void modify(EliObject parameters) throws Exception {
		SqlSession session = null;
		
		try {
			session = Database.session(true);
			
			if (session.insert("board-" + parameters.get("table") + "-update", parameters) > 0) Database.commit(session);
			else throw new Exception("Failed to create new affiliate data");
		} catch (Exception e) { Database.rollback(session); throw e; } finally { Database.release(session); }
	}
	
	protected void remove(EliObject parameters) {

	}
}

package io.loli.sc.server.action;

import io.loli.sc.server.entity.UploadedImage;
import io.loli.sc.server.entity.User;
import io.loli.sc.server.service.UploadedImageService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Consts;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Named
@RequestMapping(value = "/img")
public class ImageAction {

    @Inject
    @Named("imageService")
    private UploadedImageService imageService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listByUId(Model model, HttpServletRequest request) {
        return "redirect:/img/list/1";
    }

    @RequestMapping(value = "/list/{page}", method = RequestMethod.GET)
    public String listByUId(@PathVariable(value = "page") int page,
            Model model, HttpServletRequest request) {
        if (page == 0) {
            page = 1;
        }
        int firstPosition = (page - 1) * imageService.getMaxResults();
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        int u_id = ((User) request.getSession().getAttribute("user")).getId();

        List<UploadedImage> list = imageService.listByUId(u_id, firstPosition);
        int totalCount = imageService.countByUId(u_id);
        int pageCount = (int) Math.ceil((float) totalCount
                / (float) imageService.getMaxResults());
        boolean hasLast = page != 1;
        boolean hasNext = page != pageCount;
        int current = page;
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("pageCount", pageCount);
        request.setAttribute("hasLast", hasLast);
        request.setAttribute("hasNext", hasNext);
        request.setAttribute("currentPage", current);
        request.setAttribute("count", totalCount);
        model.addAttribute("imgList", list);
        return "image/list";
    }

    @RequestMapping(value = "/search")
    public String search(@RequestParam(value = "fileName") String fileName,
            @RequestParam(value = "page", required = false) Integer page,
            HttpServletRequest request, Model model) {
        if (page == null || page == 0) {
            page = 1;
        }
        int firstPosition = (page - 1) * imageService.getMaxResults();
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        int u_id = ((User) request.getSession().getAttribute("user")).getId();

        List<UploadedImage> list = imageService.listByUIdAndFileName(u_id,
                fileName, firstPosition);
        int totalCount = imageService.countByUIdAndFileName(u_id, fileName);
        int count = imageService.countByUId(u_id);
        int pageCount = (int) Math.ceil((float) totalCount
                / (float) imageService.getMaxResults());
        boolean hasLast = page != 1;
        boolean hasNext = page != pageCount;
        int current = page;
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("count", count);
        request.setAttribute("pageCount", pageCount);
        request.setAttribute("hasLast", hasLast);
        request.setAttribute("hasNext", hasNext);
        request.setAttribute("currentPage", current);
        request.setAttribute("fileName", fileName);
        model.addAttribute("imgList", list);
        return "image/list";
    }

    @RequestMapping(value = "/delete")
    public String delete(@RequestParam(value = "id") int id,
            RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        UploadedImage image = imageService.findById(id);
        if (user == null || image == null
                || image.getUser().getId() != user.getId()) {
            redirectAttributes.addFlashAttribute("message", "非法登陆");
        } else {
            try {
                imageService.delete(id);
                redirectAttributes.addFlashAttribute("message", "删除成功");
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message",
                        "删除失败，原因是" + e.getMessage());
            }
        }

        return "redirect:/img/list";
    }

    @RequestMapping(value = "/{filename:[a-zA-Z0-9]{1,}\\.png}")
    public void showImg(@PathVariable("filename") String filename,
            HttpServletRequest request, HttpServletResponse response) {
        String imagePath = request.getServletContext().getRealPath("img");

        File file = new File(imagePath + File.separator + filename);
        if (file.exists()) {
            response.setContentType("image/png");
            BufferedOutputStream out = null;
            BufferedInputStream in = null;
            try {
                out = new BufferedOutputStream(response.getOutputStream());
                in = new BufferedInputStream(new FileInputStream(file));
                byte[] buff = new byte[2048];
                int bytesRead;
                while (-1 != (bytesRead = in.read(buff, 0, buff.length))) {
                    out.write(buff, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null)
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        } else {
            try {
                response.getWriter().println("File does not exists");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
